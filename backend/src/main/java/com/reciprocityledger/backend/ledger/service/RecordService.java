package com.reciprocityledger.backend.ledger.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.auth.AuthSessionService;
import com.reciprocityledger.backend.common.enums.RecordTypeEnum;
import com.reciprocityledger.backend.common.enums.ReciprocityStatusEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.contact.entity.Contact;
import com.reciprocityledger.backend.contact.service.ContactService;
import com.reciprocityledger.backend.ledger.dto.request.CreateRecordRequest;
import com.reciprocityledger.backend.ledger.dto.request.RecordDetailRequest;
import com.reciprocityledger.backend.ledger.dto.request.UpdateRecordRequest;
import com.reciprocityledger.backend.ledger.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.ledger.entity.EventExchange;
import com.reciprocityledger.backend.ledger.entity.LedgerRecord;
import com.reciprocityledger.backend.ledger.mapper.EventExchangeMapper;
import com.reciprocityledger.backend.ledger.mapper.EventTypeDictMapper;
import com.reciprocityledger.backend.ledger.mapper.LedgerRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 礼账记录服务。
 * 这里负责维护 ledger_record 明细和 event_exchange 聚合快照的一致性。
 */
@Service
@RequiredArgsConstructor
public class RecordService {

    private final AuthSessionService authSessionService;
    private final ContactService contactService;
    private final EventTypeDictMapper eventTypeDictMapper;
    private final EventExchangeMapper eventExchangeMapper;
    private final LedgerRecordMapper ledgerRecordMapper;
    private final RecordAggregateCalculator recordAggregateCalculator;
    private final IdGenerator idGenerator;

    /**
     * 新增记录流程：校验参数、定位聚合、校验同类型唯一、落明细、重算聚合、刷新联系人最近往来日期。
     */
    @Transactional(rollbackFor = Exception.class)
    public RecordDetailResponse create(CreateRecordRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        Contact contact = contactService.requireContact(userId, request.getContactId());
        RecordTypeEnum recordType = RecordTypeEnum.of(request.getRecordType());
        String eventTypeCode = validateEventTypeCode(request.getEventTypeCode());
        String eventNote = normalizeEventNote(request.getEventNote());
        LocalDate occurredOn = validateOccurredOn(request.getOccurredOn());
        BigDecimal amount = validateAmount(request.getAmount());
        String remark = normalizeRemark(request.getRemark());

        EventExchange eventExchange = getOrCreateExchange(userId, contact.getId(), eventTypeCode, eventNote, occurredOn, recordType);
        validateDuplicateRecord(userId, eventExchange.getId(), recordType.name(), null);

        LedgerRecord ledgerRecord = new LedgerRecord();
        ledgerRecord.setId(idGenerator.nextId());
        ledgerRecord.setUserId(userId);
        ledgerRecord.setEventExchangeId(eventExchange.getId());
        ledgerRecord.setRecordType(recordType.name());
        ledgerRecord.setOccurredOn(occurredOn);
        ledgerRecord.setAmount(amount);
        ledgerRecord.setRemark(remark);
        ledgerRecord.setCreatedBy(userId);
        ledgerRecord.setUpdatedBy(userId);
        ledgerRecordMapper.insert(ledgerRecord);

        // 明细落库后必须立刻重算聚合，避免 event_exchange 快照与事实表不一致。
        recalculateExchange(userId, eventExchange.getId());
        refreshContactLastInteraction(userId, contact.getId());
        return requireRecordDetail(userId, ledgerRecord.getId());
    }

    /**
     * 编辑记录时要区分“仍在原聚合内”与“迁移到新聚合”两种情况，保证前后两个聚合都被正确重算。
     */
    @Transactional(rollbackFor = Exception.class)
    public RecordDetailResponse update(UpdateRecordRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        LedgerRecord currentRecord = requireRecord(userId, request.getRecordId());
        EventExchange currentExchange = requireExchange(userId, currentRecord.getEventExchangeId());
        Contact targetContact = contactService.requireContact(userId, request.getContactId());
        RecordTypeEnum targetRecordType = RecordTypeEnum.of(request.getRecordType());
        String eventTypeCode = validateEventTypeCode(request.getEventTypeCode());
        String eventNote = normalizeEventNote(request.getEventNote());
        LocalDate occurredOn = validateOccurredOn(request.getOccurredOn());
        BigDecimal amount = validateAmount(request.getAmount());
        String remark = normalizeRemark(request.getRemark());

        EventExchange targetExchange = currentExchange;
        if (!isSameExchange(currentExchange, targetContact.getId(), eventTypeCode, eventNote)) {
            targetExchange = getOrCreateExchange(userId, targetContact.getId(), eventTypeCode, eventNote, occurredOn, targetRecordType);
        }
        validateDuplicateRecord(userId, targetExchange.getId(), targetRecordType.name(), currentRecord.getId());

        currentRecord.setEventExchangeId(targetExchange.getId());
        currentRecord.setRecordType(targetRecordType.name());
        currentRecord.setOccurredOn(occurredOn);
        currentRecord.setAmount(amount);
        currentRecord.setRemark(remark);
        currentRecord.setUpdatedBy(userId);
        ledgerRecordMapper.update(currentRecord);

        recalculateExchange(userId, targetExchange.getId());
        if (!currentExchange.getId().equals(targetExchange.getId())) {
            recalculateExchange(userId, currentExchange.getId());
        }

        // 联系人最近往来日期取决于全部明细，聚合迁移后相关联系人都要刷新一次。
        refreshContactLastInteraction(userId, currentExchange.getContactId());
        if (!currentExchange.getContactId().equals(targetContact.getId())) {
            refreshContactLastInteraction(userId, targetContact.getId());
        }
        return requireRecordDetail(userId, currentRecord.getId());
    }

    public RecordDetailResponse detail(RecordDetailRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        return requireRecordDetail(userId, request.getRecordId());
    }

    /**
     * event_exchange 是“联系人 + 事件类型 + 事件说明”的聚合根，不存在时按当前记录类型初始化默认回礼状态。
     */
    private EventExchange getOrCreateExchange(Long userId, Long contactId, String eventTypeCode, String eventNote, LocalDate occurredOn, RecordTypeEnum recordType) {
        EventExchange eventExchange = eventExchangeMapper.selectByKey(userId, contactId, eventTypeCode, eventNote);
        if (eventExchange != null) {
            return eventExchange;
        }
        EventExchange newExchange = new EventExchange();
        newExchange.setId(idGenerator.nextId());
        newExchange.setUserId(userId);
        newExchange.setContactId(contactId);
        newExchange.setEventTypeCode(eventTypeCode);
        newExchange.setEventNote(eventNote);
        newExchange.setGiveAmount(BigDecimal.ZERO);
        newExchange.setReceiveAmount(BigDecimal.ZERO);
        newExchange.setLatestOccurredOn(occurredOn);
        newExchange.setReciprocityStatus(recordType == RecordTypeEnum.GIVE ? ReciprocityStatusEnum.WAIT_OTHER.name() : ReciprocityStatusEnum.WAIT_ME.name());
        newExchange.setCreatedBy(userId);
        newExchange.setUpdatedBy(userId);
        eventExchangeMapper.insert(newExchange);
        return newExchange;
    }

    /**
     * 当前版本约束“同一聚合下每种记录类型最多一条”，因此编辑时允许忽略自己，新增时不允许重复。
     */
    private void validateDuplicateRecord(Long userId, Long eventExchangeId, String recordType, Long ignoreRecordId) {
        LedgerRecord duplicatedRecord = ledgerRecordMapper.selectByExchangeIdAndType(userId, eventExchangeId, recordType);
        if (duplicatedRecord != null && !duplicatedRecord.getId().equals(ignoreRecordId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RECORD, "同一联系人同一事件下该记录类型已存在");
        }
    }

    /**
     * 重算逻辑统一从明细表反推快照，避免在多个写路径里手工拼装聚合字段。
     */
    private void recalculateExchange(Long userId, Long eventExchangeId) {
        List<LedgerRecord> records = ledgerRecordMapper.selectByExchangeId(userId, eventExchangeId);
        if (records == null || records.isEmpty()) {
            eventExchangeMapper.deleteById(userId, eventExchangeId);
            return;
        }
        EventExchangeSnapshot snapshot = recordAggregateCalculator.calculate(records);
        EventExchange eventExchange = requireExchange(userId, eventExchangeId);
        eventExchange.setGiveRecordId(snapshot.getGiveRecordId());
        eventExchange.setReceiveRecordId(snapshot.getReceiveRecordId());
        eventExchange.setGiveAmount(snapshot.getGiveAmount());
        eventExchange.setReceiveAmount(snapshot.getReceiveAmount());
        eventExchange.setLatestOccurredOn(snapshot.getLatestOccurredOn());
        eventExchange.setReciprocityStatus(snapshot.getReciprocityStatus());
        eventExchange.setUpdatedBy(userId);
        eventExchangeMapper.updateSnapshot(eventExchange);
    }

    /**
     * 联系人最近往来日期按该联系人全部礼账明细的最大发生日期计算。
     */
    private void refreshContactLastInteraction(Long userId, Long contactId) {
        LocalDate latestOccurredOn = ledgerRecordMapper.selectMaxOccurredOnByContact(userId, contactId);
        contactService.refreshLastInteractionOn(userId, contactId, latestOccurredOn);
    }

    private boolean isSameExchange(EventExchange currentExchange, Long contactId, String eventTypeCode, String eventNote) {
        return currentExchange.getContactId().equals(contactId)
                && currentExchange.getEventTypeCode().equals(eventTypeCode)
                && currentExchange.getEventNote().equals(eventNote);
    }

    /**
     * 事件类型必须来自启用中的字典，避免后续统计口径被随意输入打散。
     */
    private String validateEventTypeCode(String eventTypeCode) {
        String normalizedCode = StrUtil.trim(eventTypeCode);
        if (StrUtil.isBlank(normalizedCode)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "eventTypeCode不能为空");
        }
        if (eventTypeDictMapper.countEnabledByCode(normalizedCode) < 1) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "eventTypeCode不存在或未启用");
        }
        return normalizedCode;
    }

    /**
     * eventNote 参与聚合唯一键，空值统一归一为 ""，避免 null 和空串形成两套聚合。
     */
    private String normalizeEventNote(String eventNote) {
        String normalizedValue = StrUtil.trim(eventNote);
        if (StrUtil.isBlank(normalizedValue)) {
            return "";
        }
        if (normalizedValue.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "eventNote长度不能超过200");
        }
        return normalizedValue;
    }

    private String normalizeRemark(String remark) {
        String normalizedValue = StrUtil.trim(remark);
        if (StrUtil.isBlank(normalizedValue)) {
            return null;
        }
        if (normalizedValue.length() > 500) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "remark长度不能超过500");
        }
        return normalizedValue;
    }

    private LocalDate validateOccurredOn(LocalDate occurredOn) {
        if (occurredOn == null) {
            throw new BusinessException(ErrorCode.INVALID_DATE, "occurredOn不能为空");
        }
        if (occurredOn.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_DATE, "occurredOn不能晚于今天");
        }
        return occurredOn;
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "amount不能为空");
        }
        if (amount.scale() > 2) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "amount最多保留2位小数");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "amount必须大于0");
        }
        return amount;
    }

    private LedgerRecord requireRecord(Long userId, Long recordId) {
        LedgerRecord ledgerRecord = ledgerRecordMapper.selectById(userId, recordId);
        if (ledgerRecord == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        return ledgerRecord;
    }

    private EventExchange requireExchange(Long userId, Long exchangeId) {
        EventExchange eventExchange = eventExchangeMapper.selectById(userId, exchangeId);
        if (eventExchange == null) {
            throw new BusinessException(ErrorCode.EVENT_EXCHANGE_NOT_FOUND, "事件聚合不存在");
        }
        return eventExchange;
    }

    private RecordDetailResponse requireRecordDetail(Long userId, Long recordId) {
        RecordDetailResponse response = ledgerRecordMapper.selectDetailById(userId, recordId);
        if (response == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        return response;
    }
}
