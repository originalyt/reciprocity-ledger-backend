package com.reciprocityledger.backend.event.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.enums.EventOwnerTypeEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.contact.service.ContactService;
import com.reciprocityledger.backend.dict.entity.EventTypeDict;
import com.reciprocityledger.backend.dict.service.DictService;
import com.reciprocityledger.backend.event.dto.request.EventDetailRequest;
import com.reciprocityledger.backend.event.dto.request.EventPageRequest;
import com.reciprocityledger.backend.event.dto.request.EventSaveRequest;
import com.reciprocityledger.backend.event.dto.request.EventUpdateRequest;
import com.reciprocityledger.backend.event.dto.response.EventByContactResponse;
import com.reciprocityledger.backend.event.dto.response.EventDetailResponse;
import com.reciprocityledger.backend.event.dto.response.EventPageItemResponse;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import com.reciprocityledger.backend.event.mapper.EventMapper;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;
    private final DictService dictService;
    private final ContactService contactService;
    private final RecordMapper recordMapper;
    private final IdGenerator idGenerator;

    public PageResponse<EventPageItemResponse> page(EventPageRequest request) {
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String keyword = normalizeNullable(request.getKeyword());
        String eventTypeCode = normalizeNullable(request.getEventTypeCode());
        String eventOwnerType = normalizeNullable(request.getEventOwnerType());
        String ownerContactId = normalizeNullable(request.getOwnerContactId());
        long total = eventMapper.countPage(keyword, eventTypeCode, eventOwnerType, ownerContactId, request.getStartDate(), request.getEndDate());
        List<EventPageItemResponse> list = eventMapper.selectPage(keyword, eventTypeCode, eventOwnerType, ownerContactId, request.getStartDate(), request.getEndDate(), PageUtils.offset(pageNo, pageSize), pageSize);
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    public EventDetailResponse detail(EventDetailRequest request) {
        GiftEvent event = requireEvent(request.getEventId());
        return buildDetail(event);
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse save(EventSaveRequest request) {
        GiftEvent event = new GiftEvent();
        event.setId(idGenerator.nextId());
        fillEvent(event, request.getEventName(), request.getEventTypeId(), request.getEventOwnerType(), request.getOwnerContactId(), request.getEventDate(), request.getRemark());
        event.setStatus("NORMAL");
        eventMapper.insert(event);
        return new IdResponse(event.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse quickSave(String eventName, String eventTypeId, String eventOwnerType, String ownerContactId, java.time.LocalDate eventDate, String remark) {
        GiftEvent event = new GiftEvent();
        event.setId(idGenerator.nextId());
        fillEvent(event, eventName, eventTypeId, eventOwnerType, ownerContactId, eventDate, remark);
        event.setStatus("NORMAL");
        eventMapper.insert(event);
        return new IdResponse(event.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public IdResponse update(EventUpdateRequest request) {
        GiftEvent event = requireEvent(request.getEventId());
        fillEvent(event, request.getEventName(), request.getEventTypeId(), request.getEventOwnerType(), request.getOwnerContactId(), request.getEventDate(), request.getRemark());
        eventMapper.update(event);
        return new IdResponse(event.getId());
    }

    public GiftEvent requireEvent(String eventId) {
        GiftEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND, "事件不存在");
        }
        return event;
    }

    public List<EventPageItemResponse> recentEvents(int limit) {
        return eventMapper.selectRecent(limit);
    }

    public EventByContactResponse eventsByContact(String contactId) {
        contactService.requireContact(contactId);
        EventByContactResponse response = new EventByContactResponse();
        response.setContactId(contactId);
        response.setContactName(contactService.getContactName(contactId));
        response.setSelfEventList(eventMapper.selectByContact("SELF", null));
        response.setContactEventList(eventMapper.selectByContact("CONTACT", contactId));
        return response;
    }

    private EventDetailResponse buildDetail(GiftEvent event) {
        EventPageItemResponse summary = eventMapper.selectSummaryById(event.getId());
        EventDetailResponse response = new EventDetailResponse();
        response.setEventId(event.getId());
        response.setEventName(event.getEventName());
        response.setEventTypeId(event.getEventTypeId());
        response.setEventOwnerType(event.getEventOwnerType());
        response.setOwnerContactId(event.getOwnerContactId());
        response.setEventDate(event.getEventDate());
        response.setRemark(event.getRemark());
        if (summary != null) {
            response.setEventTypeCode(summary.getEventTypeCode());
            response.setEventTypeName(summary.getEventTypeName());
            response.setOwnerContactName(summary.getOwnerContactName());
            response.setRecordCount(summary.getRecordCount());
        }
        BigDecimal receiveTotal = recordMapper.sumAmountByEventAndDirection(event.getId(), "RECEIVE");
        BigDecimal sendTotal = recordMapper.sumAmountByEventAndDirection(event.getId(), "SEND");
        response.setReceiveTotalAmount(receiveTotal == null ? BigDecimal.ZERO : receiveTotal);
        response.setSendTotalAmount(sendTotal == null ? BigDecimal.ZERO : sendTotal);
        Long contactCount = recordMapper.countDistinctContactByEventId(event.getId());
        response.setContactCount(contactCount == null ? 0L : contactCount);
        return response;
    }

    private void fillEvent(GiftEvent event, String eventName, String eventTypeId, String eventOwnerType, String ownerContactId, java.time.LocalDate eventDate, String remark) {
        event.setEventName(normalizeRequired(eventName, "eventName不能为空", 128));
        EventTypeDict eventType = dictService.getEventType(eventTypeId);
        if (eventType == null || !Boolean.TRUE.equals(eventType.getEnabledFlag())) {
            throw new BusinessException(ErrorCode.EVENT_TYPE_NOT_FOUND, "事件类型不存在或已停用");
        }
        event.setEventTypeId(eventTypeId);
        String normalizedOwnerType = normalizeRequired(eventOwnerType, "eventOwnerType不能为空", 16);
        if (!EventOwnerTypeEnum.SELF.name().equals(normalizedOwnerType) && !EventOwnerTypeEnum.CONTACT.name().equals(normalizedOwnerType)) {
            throw new BusinessException(ErrorCode.INVALID_EVENT_OWNER, "事件归属类型不合法");
        }
        event.setEventOwnerType(normalizedOwnerType);
        String normalizedOwnerContactId = normalizeNullable(ownerContactId);
        if (EventOwnerTypeEnum.CONTACT.name().equals(normalizedOwnerType)) {
            if (StrUtil.isBlank(normalizedOwnerContactId)) {
                throw new BusinessException(ErrorCode.INVALID_EVENT_OWNER, "联系人事件必须选择ownerContactId");
            }
            contactService.requireContact(normalizedOwnerContactId);
            event.setOwnerContactId(normalizedOwnerContactId);
        } else {
            event.setOwnerContactId(null);
        }
        event.setEventDate(eventDate);
        event.setRemark(normalizeNullableLength(remark, 500));
    }

    private String normalizeRequired(String value, String message, int maxLength) {
        String normalized = StrUtil.trim(value);
        if (StrUtil.isBlank(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, message);
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "字段长度超出限制");
        }
        return normalized;
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

