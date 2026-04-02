package com.reciprocityledger.backend.event.service;

import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.enums.EventOwnerTypeEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.event.dto.request.EventRelationDeleteRequest;
import com.reciprocityledger.backend.event.dto.request.EventRelationSaveRequest;
import com.reciprocityledger.backend.event.dto.request.SuggestRelationRequest;
import com.reciprocityledger.backend.event.dto.request.UnlinkedEventsRequest;
import com.reciprocityledger.backend.event.dto.response.EventRelationVO;
import com.reciprocityledger.backend.event.dto.response.SuggestRelationVO;
import com.reciprocityledger.backend.event.dto.response.UnlinkedEventVO;
import com.reciprocityledger.backend.event.entity.EventRelation;
import com.reciprocityledger.backend.event.entity.GiftEvent;
import com.reciprocityledger.backend.event.mapper.EventRelationMapper;
import com.reciprocityledger.backend.user.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRelationService {

    private final EventRelationMapper eventRelationMapper;
    private final EventService eventService;
    private final IdGenerator idGenerator;

    /**
     * 创建事件关联。
     */
    @Transactional(rollbackFor = Exception.class)
    public IdResponse save(EventRelationSaveRequest request) {
        String userId = UserContext.getUserId();
        String selfEventId = request.getSelfEventId();
        String contactEventId = request.getContactEventId();

        // 校验self_event是SELF类型
        GiftEvent selfEvent = eventService.requireEvent(selfEventId);
        if (!EventOwnerTypeEnum.SELF.name().equals(selfEvent.getEventOwnerType())) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_OWNER_TYPE_INVALID, "本人事件必须是SELF类型");
        }

        // 校验contact_event是CONTACT类型
        GiftEvent contactEvent = eventService.requireEvent(contactEventId);
        if (!EventOwnerTypeEnum.CONTACT.name().equals(contactEvent.getEventOwnerType())) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_OWNER_TYPE_INVALID, "联系人事件必须是CONTACT类型");
        }

        // 校验两个事件类型相同
        if (!selfEvent.getEventTypeId().equals(contactEvent.getEventTypeId())) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_TYPE_MISMATCH, "事件类型不匹配，只能关联相同类型的事件");
        }

        // 校验contact_event未被其他SELF事件关联
        String existingRelationId = eventRelationMapper.selectIdByContactEventId(contactEventId);
        if (existingRelationId != null) {
            throw new BusinessException(ErrorCode.CONTACT_EVENT_ALREADY_LINKED, "该联系人事件已关联其他本人事件");
        }

        // 校验关联不存在
        EventRelation existing = eventRelationMapper.selectByEvents(selfEventId, contactEventId);
        if (existing != null) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_EXISTS, "事件关联已存在");
        }

        // 创建关联记录
        EventRelation relation = new EventRelation();
        relation.setId(idGenerator.nextId());
        relation.setSelfEventId(selfEventId);
        relation.setContactEventId(contactEventId);
        relation.setUserId(userId);
        eventRelationMapper.insert(relation);

        return new IdResponse(relation.getId());
    }

    /**
     * 删除事件关联。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(EventRelationDeleteRequest request) {
        String userId = UserContext.getUserId();
        EventRelation relation = eventRelationMapper.selectById(request.getId());
        if (relation == null || !userId.equals(relation.getUserId())) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_NOT_FOUND, "事件关联不存在");
        }
        eventRelationMapper.deleteById(request.getId(), userId);
    }

    /**
     * 查询事件的关联列表。
     */
    public ListResponse<EventRelationVO> list(String selfEventId) {
        String userId = UserContext.getUserId();
        // 校验selfEvent存在
        eventService.requireEvent(selfEventId);
        List<EventRelationVO> list = eventRelationMapper.selectRelationList(selfEventId, userId);
        return ListResponse.of(list);
    }

    /**
     * 查询可关联事件建议。
     */
    public ListResponse<SuggestRelationVO> suggest(SuggestRelationRequest request) {
        String userId = UserContext.getUserId();
        // 校验selfEvent存在并获取事件类型
        GiftEvent selfEvent = eventService.requireEvent(request.getSelfEventId());
        if (!EventOwnerTypeEnum.SELF.name().equals(selfEvent.getEventOwnerType())) {
            throw new BusinessException(ErrorCode.EVENT_RELATION_OWNER_TYPE_INVALID, "只能查询SELF类型事件的可关联建议");
        }
        List<SuggestRelationVO> list = eventRelationMapper.selectSuggestRelations(selfEvent.getEventTypeId(), userId);
        return ListResponse.of(list);
    }

    /**
     * 查询未关联事件（待还提醒）。
     */
    public PageResponse<UnlinkedEventVO> unlinked(UnlinkedEventsRequest request) {
        String userId = UserContext.getUserId();
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String eventTypeId = request.getEventTypeId();

        long total = eventRelationMapper.countUnlinkedEvents(userId, eventTypeId);
        List<UnlinkedEventVO> list = eventRelationMapper.selectUnlinkedEvents(userId, eventTypeId, PageUtils.offset(pageNo, pageSize), pageSize);
        return PageResponse.of(list, pageNo, pageSize, total);
    }
}
