package com.reciprocityledger.backend.reciprocity.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.enums.MatchStatusEnum;
import com.reciprocityledger.backend.common.enums.MatchTypeEnum;
import com.reciprocityledger.backend.common.enums.ReciprocityStatusEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import com.reciprocityledger.backend.event.service.EventService;
import com.reciprocityledger.backend.record.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import com.reciprocityledger.backend.record.entity.GiftRecord;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityDetailRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityHistoryReferenceRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityManualCancelRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityManualConfirmRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityPageRequest;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityDetailResponse;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityHistoryReferenceResponse;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityMatchInfoResponse;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityPageItemResponse;
import com.reciprocityledger.backend.reciprocity.entity.ReciprocityMatch;
import com.reciprocityledger.backend.reciprocity.mapper.ReciprocityMatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReciprocityService {

    private final ReciprocityMatchMapper reciprocityMatchMapper;
    private final RecordMapper recordMapper;
    private final EventService eventService;
    private final IdGenerator idGenerator;

    public PageResponse<ReciprocityPageItemResponse> page(ReciprocityPageRequest request) {
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String contactId = normalizeNullable(request.getContactId());
        String eventTypeCode = normalizeNullable(request.getEventTypeCode());
        String reciprocityStatus = normalizeNullable(request.getReciprocityStatus());
        List<ReciprocityPageItemResponse> list = reciprocityMatchMapper.selectPage(contactId, eventTypeCode, reciprocityStatus, request.getStartDate(), request.getEndDate(), PageUtils.offset(pageNo, pageSize), pageSize);
        long total = reciprocityMatchMapper.countPage(contactId, eventTypeCode, reciprocityStatus, request.getStartDate(), request.getEndDate());
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    public ReciprocityDetailResponse detail(ReciprocityDetailRequest request) {
        RecordDetailResponse recordInfo = recordMapper.selectDetailById(request.getRecordId());
        if (recordInfo == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        ReciprocityDetailResponse response = new ReciprocityDetailResponse();
        response.setRecordInfo(recordInfo);
        ReciprocityMatch activeMatch = reciprocityMatchMapper.selectActiveByRecordId(request.getRecordId());
        if (activeMatch != null) {
            String matchedRecordId = StrUtil.equals(activeMatch.getSourceRecordId(), request.getRecordId()) ? activeMatch.getTargetRecordId() : activeMatch.getSourceRecordId();
            response.setMatchedRecordInfo(recordMapper.selectDetailById(matchedRecordId));
            ReciprocityMatchInfoResponse matchInfo = new ReciprocityMatchInfoResponse();
            matchInfo.setReciprocityMatchId(activeMatch.getId());
            matchInfo.setMatchType(activeMatch.getMatchType());
            matchInfo.setMatchStatus(activeMatch.getMatchStatus());
            matchInfo.setCancelReason(activeMatch.getCancelReason());
            matchInfo.setRemark(activeMatch.getRemark());
            response.setManualFlagInfo(matchInfo);
        }
        ReciprocityHistoryReferenceRequest historyRequest = new ReciprocityHistoryReferenceRequest();
        historyRequest.setContactId(recordInfo.getContactId());
        historyRequest.setEventTypeId(recordInfo.getEventTypeId());
        response.setHistoryReference(historyReference(historyRequest));
        return response;
    }

    public ReciprocityHistoryReferenceResponse historyReference(ReciprocityHistoryReferenceRequest request) {
        BigDecimal receiveAmount = recordMapper.sumAmountByContactAndEventTypeAndDirection(request.getContactId(), request.getEventTypeId(), "RECEIVE");
        BigDecimal sendAmount = recordMapper.sumAmountByContactAndEventTypeAndDirection(request.getContactId(), request.getEventTypeId(), "SEND");
        Long unclosedCount = recordMapper.countByContactAndEventTypeAndStatus(request.getContactId(), request.getEventTypeId(), ReciprocityStatusEnum.UNMATCHED.name());
        RecordPageItemResponse lastRecord = recordMapper.selectLastRecordByContactAndEventType(request.getContactId(), request.getEventTypeId());
        ReciprocityHistoryReferenceResponse response = new ReciprocityHistoryReferenceResponse();
        response.setSameTypeReceiveAmount(receiveAmount == null ? BigDecimal.ZERO : receiveAmount);
        response.setSameTypeSendAmount(sendAmount == null ? BigDecimal.ZERO : sendAmount);
        response.setUnclosedRecordCount(unclosedCount == null ? 0L : unclosedCount);
        response.setLastSameTypeRecord(lastRecord);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse manualConfirm(ReciprocityManualConfirmRequest request) {
        if (StrUtil.equals(request.getSourceRecordId(), request.getTargetRecordId())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "不能将同一条记录确认为闭环");
        }
        GiftRecord sourceRecord = requireRecord(request.getSourceRecordId());
        GiftRecord targetRecord = requireRecord(request.getTargetRecordId());
        validateManualMatch(sourceRecord, targetRecord);
        if (reciprocityMatchMapper.selectActiveByRecordId(sourceRecord.getId()) != null || reciprocityMatchMapper.selectActiveByRecordId(targetRecord.getId()) != null) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "存在有效闭环，不能重复确认");
        }
        ReciprocityMatch match = new ReciprocityMatch();
        match.setId(idGenerator.nextId());
        match.setSourceRecordId(sourceRecord.getId());
        match.setTargetRecordId(targetRecord.getId());
        match.setMatchType(MatchTypeEnum.MANUAL.name());
        match.setMatchStatus(MatchStatusEnum.ACTIVE.name());
        match.setRemark(normalizeNullable(request.getRemark()));
        reciprocityMatchMapper.insert(match);
        recordMapper.updateReciprocityStatus(sourceRecord.getId(), ReciprocityStatusEnum.MANUAL_CONFIRMED.name());
        recordMapper.updateReciprocityStatus(targetRecord.getId(), ReciprocityStatusEnum.MANUAL_CONFIRMED.name());
        return new IdResponse(match.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse manualCancel(ReciprocityManualCancelRequest request) {
        ReciprocityMatch match = reciprocityMatchMapper.selectById(request.getReciprocityMatchId());
        if (match == null) {
            throw new BusinessException(ErrorCode.RECIPROCITY_MATCH_NOT_FOUND, "闭环关系不存在");
        }
        if (!MatchStatusEnum.ACTIVE.name().equals(match.getMatchStatus())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "当前闭环关系已经失效");
        }
        String reason = normalizeNullable(request.getCancelReason());
        reciprocityMatchMapper.cancel(match.getId(), reason);
        recordMapper.updateReciprocityStatus(match.getSourceRecordId(), ReciprocityStatusEnum.MANUAL_CANCELED.name());
        recordMapper.updateReciprocityStatus(match.getTargetRecordId(), ReciprocityStatusEnum.MANUAL_CANCELED.name());
        return new IdResponse(match.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rebuildForRecord(String recordId) {
        GiftRecord currentRecord = requireRecord(recordId);
        releaseMatchByRecordId(recordId, false, null);
        GiftEvent currentEvent = eventService.requireEvent(currentRecord.getEventId());
        GiftRecord candidateRecord = recordMapper.selectLatestAutoMatchCandidate(currentRecord.getId(), currentRecord.getContactId(), currentEvent.getEventTypeId(), currentRecord.getDirection(), currentEvent.getEventOwnerType());
        if (candidateRecord == null) {
            if (!ReciprocityStatusEnum.MANUAL_CANCELED.name().equals(currentRecord.getReciprocityStatus())) {
                recordMapper.updateReciprocityStatus(currentRecord.getId(), ReciprocityStatusEnum.UNMATCHED.name());
            }
            return;
        }
        ReciprocityMatch match = new ReciprocityMatch();
        match.setId(idGenerator.nextId());
        match.setSourceRecordId(currentRecord.getId());
        match.setTargetRecordId(candidateRecord.getId());
        match.setMatchType(MatchTypeEnum.AUTO.name());
        match.setMatchStatus(MatchStatusEnum.ACTIVE.name());
        reciprocityMatchMapper.insert(match);
        recordMapper.updateReciprocityStatus(currentRecord.getId(), ReciprocityStatusEnum.MATCHED.name());
        recordMapper.updateReciprocityStatus(candidateRecord.getId(), ReciprocityStatusEnum.MATCHED.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseMatchByRecordId(String recordId, boolean manualCancel, String cancelReason) {
        ReciprocityMatch activeMatch = reciprocityMatchMapper.selectActiveByRecordId(recordId);
        if (activeMatch == null) {
            return;
        }
        reciprocityMatchMapper.cancel(activeMatch.getId(), cancelReason);
        String selfStatus = manualCancel ? ReciprocityStatusEnum.MANUAL_CANCELED.name() : ReciprocityStatusEnum.UNMATCHED.name();
        String otherStatus = manualCancel ? ReciprocityStatusEnum.MANUAL_CANCELED.name() : ReciprocityStatusEnum.UNMATCHED.name();
        recordMapper.updateReciprocityStatus(activeMatch.getSourceRecordId(), selfStatus);
        recordMapper.updateReciprocityStatus(activeMatch.getTargetRecordId(), otherStatus);
    }

    private void validateManualMatch(GiftRecord sourceRecord, GiftRecord targetRecord) {
        if (!StrUtil.equals(sourceRecord.getContactId(), targetRecord.getContactId())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "两条记录联系人必须一致");
        }
        GiftEvent sourceEvent = eventService.requireEvent(sourceRecord.getEventId());
        GiftEvent targetEvent = eventService.requireEvent(targetRecord.getEventId());
        if (!StrUtil.equals(sourceEvent.getEventTypeId(), targetEvent.getEventTypeId())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "两条记录事件类型必须一致");
        }
        if (StrUtil.equals(sourceRecord.getDirection(), targetRecord.getDirection())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "两条记录方向必须相反");
        }
        if (StrUtil.equals(sourceEvent.getEventOwnerType(), targetEvent.getEventOwnerType())) {
            throw new BusinessException(ErrorCode.INVALID_RECIPROCITY_OPERATION, "闭环记录必须分别来自本人事件和联系人事件");
        }
    }

    private GiftRecord requireRecord(String recordId) {
        GiftRecord record = recordMapper.selectEntityById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND, "记录不存在");
        }
        return record;
    }

    private String normalizeNullable(String value) {
        String normalized = StrUtil.trim(value);
        return StrUtil.isBlank(normalized) ? null : normalized;
    }
}
