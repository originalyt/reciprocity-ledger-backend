package com.reciprocityledger.backend.record.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.enums.EventOwnerTypeEnum;
import com.reciprocityledger.backend.common.enums.RecordDirectionEnum;
import com.reciprocityledger.backend.common.enums.ReciprocityStatusEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.contact.entity.Contact;
import com.reciprocityledger.backend.contact.service.ContactService;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import com.reciprocityledger.backend.event.service.EventService;
import com.reciprocityledger.backend.reciprocity.service.ReciprocityService;
import com.reciprocityledger.backend.record.dto.request.ContactTimelineRequest;
import com.reciprocityledger.backend.record.dto.request.RecordDetailRequest;
import com.reciprocityledger.backend.record.dto.request.RecordPageRequest;
import com.reciprocityledger.backend.record.dto.request.RecordQuickSaveContactRequest;
import com.reciprocityledger.backend.record.dto.request.RecordSaveRequest;
import com.reciprocityledger.backend.record.dto.request.RecordSaveWithEventRequest;
import com.reciprocityledger.backend.record.dto.request.RecordUpdateRequest;
import com.reciprocityledger.backend.record.dto.request.SelfTimelineRequest;
import com.reciprocityledger.backend.record.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import com.reciprocityledger.backend.record.dto.response.RecordQuickSaveContactResponse;
import com.reciprocityledger.backend.record.dto.response.TimelineResponse;
import com.reciprocityledger.backend.record.dto.response.TimelineSummaryResponse;
import com.reciprocityledger.backend.record.entity.GiftRecord;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import com.reciprocityledger.backend.user.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordMapper recordMapper;
    private final ContactService contactService;
    private final EventService eventService;
    private final ReciprocityService reciprocityService;
    private final IdGenerator idGenerator;

    public PageResponse<RecordPageItemResponse> page(RecordPageRequest request) {
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String userId = UserContext.getUserId();
        List<RecordPageItemResponse> list = recordMapper.selectPage(
                userId,
                normalizeNullable(request.getContactId()),
                normalizeNullable(request.getEventId()),
                normalizeDirection(request.getDirection(), false),
                normalizeNullable(request.getEventTypeCode()),
                request.getStartDate(),
                request.getEndDate(),
                PageUtils.offset(pageNo, pageSize),
                pageSize
        );
        long total = recordMapper.countPage(
                userId,
                normalizeNullable(request.getContactId()),
                normalizeNullable(request.getEventId()),
                normalizeDirection(request.getDirection(), false),
                normalizeNullable(request.getEventTypeCode()),
                request.getStartDate(),
                request.getEndDate()
        );
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    public RecordDetailResponse detail(RecordDetailRequest request) {
        RecordDetailResponse detail = recordMapper.selectDetailById(request.getRecordId());
        if (detail == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse save(RecordSaveRequest request) {
        contactService.requireContact(request.getContactId());

        // 如果没有eventId，创建一个默认事件
        String eventId = request.getEventId();
        if (StrUtil.isBlank(eventId)) {
            // 根据direction确定事件归属类型
            String direction = normalizeDirection(request.getDirection(), true);
            String eventOwnerType = RecordDirectionEnum.SEND.name().equals(direction)
                    ? EventOwnerTypeEnum.CONTACT.name()
                    : EventOwnerTypeEnum.SELF.name();

            // 创建默认事件
            IdResponse eventResponse = eventService.quickSave(
                    RecordDirectionEnum.SEND.name().equals(direction) ? "随礼" : "收礼",
                    null, // eventTypeId 可以为空，后端会处理
                    eventOwnerType,
                    RecordDirectionEnum.SEND.name().equals(direction) ? request.getContactId() : null,
                    request.getRecordDate(),
                    null
            );
            eventId = eventResponse.getId();
        } else {
            GiftEvent event = eventService.requireEvent(eventId);
            validateRecordAgainstEvent(request.getContactId(), event);
        }

        GiftRecord record = new GiftRecord();
        record.setId(idGenerator.nextId());
        record.setContactId(request.getContactId());
        record.setEventId(eventId);
        record.setDirection(normalizeDirection(request.getDirection(), true));
        record.setAmount(normalizeAmount(request.getAmount()));
        record.setRecordDate(request.getRecordDate());
        record.setRemark(normalizeNullableLength(request.getRemark(), 500));
        record.setReciprocityStatus(ReciprocityStatusEnum.UNMATCHED.name());
        record.setUserId(UserContext.getUserId());
        recordMapper.insert(record);
        reciprocityService.rebuildForRecord(record.getId());
        return new IdResponse(record.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse saveWithEvent(RecordSaveWithEventRequest request) {
        contactService.requireContact(request.getContactId());
        IdResponse eventResponse = eventService.quickSave(
                request.getEventName(),
                request.getEventTypeId(),
                request.getEventOwnerType(),
                request.getOwnerContactId(),
                request.getEventDate(),
                request.getEventRemark()
        );
        RecordSaveRequest saveRequest = new RecordSaveRequest();
        saveRequest.setContactId(request.getContactId());
        saveRequest.setEventId(eventResponse.getId());
        saveRequest.setDirection(request.getDirection());
        saveRequest.setAmount(request.getAmount());
        saveRequest.setRecordDate(request.getRecordDate());
        saveRequest.setRemark(request.getRecordRemark());
        return save(saveRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse update(RecordUpdateRequest request) {
        GiftRecord record = requireRecord(request.getRecordId());
        contactService.requireContact(request.getContactId());
        GiftEvent event = eventService.requireEvent(request.getEventId());
        validateRecordAgainstEvent(request.getContactId(), event);
        reciprocityService.releaseMatchByRecordId(record.getId(), false, null);
        record.setContactId(request.getContactId());
        record.setEventId(request.getEventId());
        record.setDirection(normalizeDirection(request.getDirection(), true));
        record.setAmount(normalizeAmount(request.getAmount()));
        record.setRecordDate(request.getRecordDate());
        record.setRemark(normalizeNullableLength(request.getRemark(), 500));
        recordMapper.update(record);
        recordMapper.updateReciprocityStatus(record.getId(), ReciprocityStatusEnum.UNMATCHED.name());
        reciprocityService.rebuildForRecord(record.getId());
        return new IdResponse(record.getId());
    }

    public TimelineResponse selfTimeline(SelfTimelineRequest request) {
        return buildTimeline(null, request.getDirection(), request.getEventTypeCode(), request.getStartDate(), request.getEndDate(), request.getPageNo(), request.getPageSize());
    }

    public TimelineResponse contactTimeline(ContactTimelineRequest request) {
        contactService.requireContact(request.getContactId());
        return buildTimeline(request.getContactId(), request.getDirection(), request.getEventTypeCode(), request.getStartDate(), request.getEndDate(), request.getPageNo(), request.getPageSize());
    }

    public GiftRecord requireRecord(String recordId) {
        GiftRecord record = recordMapper.selectEntityById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        String userId = UserContext.getUserId();
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        return record;
    }

    public RecordQuickSaveContactResponse quickSaveContact(RecordQuickSaveContactRequest request) {
        IdResponse response = contactService.quickSave(request);
        Contact contact = contactService.requireContact(response.getId());
        return new RecordQuickSaveContactResponse(
                contact.getId(),
                contact.getContactName(),
                contact.getAliasName(),
                contact.getRelationType()
        );
    }

    private TimelineResponse buildTimeline(String contactId, String direction, String eventTypeCode, java.time.LocalDate startDate, java.time.LocalDate endDate, Integer pageNoValue, Integer pageSizeValue) {
        int pageNo = PageUtils.safePageNo(pageNoValue);
        int pageSize = PageUtils.safePageSize(pageSizeValue);
        String normalizedDirection = normalizeDirection(direction, false);
        String normalizedEventTypeCode = normalizeNullable(eventTypeCode);
        String userId = UserContext.getUserId();
        List<RecordPageItemResponse> list = recordMapper.selectPage(userId, contactId, null, normalizedDirection, normalizedEventTypeCode, startDate, endDate, PageUtils.offset(pageNo, pageSize), pageSize);
        long total = recordMapper.countPage(userId, contactId, null, normalizedDirection, normalizedEventTypeCode, startDate, endDate);
        TimelineSummaryResponse summary = recordMapper.selectTimelineSummary(userId, contactId, normalizedDirection, normalizedEventTypeCode, startDate, endDate);
        if (summary == null) {
            summary = new TimelineSummaryResponse();
        }
        if (summary.getReceiveTotalAmount() == null) {
            summary.setReceiveTotalAmount(BigDecimal.ZERO);
        }
        if (summary.getSendTotalAmount() == null) {
            summary.setSendTotalAmount(BigDecimal.ZERO);
        }
        if (summary.getNetAmount() == null) {
            summary.setNetAmount(BigDecimal.ZERO);
        }
        TimelineResponse response = new TimelineResponse();
        response.setSummaryInfo(summary);
        response.setPageResult(PageResponse.of(list, pageNo, pageSize, total));
        return response;
    }

    private void validateRecordAgainstEvent(String contactId, GiftEvent event) {
        if (EventOwnerTypeEnum.CONTACT.name().equals(event.getEventOwnerType()) && !StrUtil.equals(contactId, event.getOwnerContactId())) {
            throw new BusinessException(ErrorCode.INVALID_EVENT_OWNER, "联系人事件只能绑定事件归属联系人");
        }
    }

    private String normalizeDirection(String direction, boolean required) {
        String normalized = normalizeNullable(direction);
        if (StrUtil.isBlank(normalized)) {
            if (required) {
                throw new BusinessException(ErrorCode.INVALID_DIRECTION, "direction不能为空");
            }
            return null;
        }
        if (!RecordDirectionEnum.RECEIVE.name().equals(normalized) && !RecordDirectionEnum.SEND.name().equals(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_DIRECTION, "direction不合法");
        }
        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "amount必须大于0");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNullable(String value) {
        String normalized = StrUtil.trim(value);
        return StrUtil.isBlank(normalized) ? null : normalized;
    }

    private String normalizeNullableLength(String value, int maxLength) {
        String normalized = normalizeNullable(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "字段长度超出限制");
        }
        return normalized;
    }
}


