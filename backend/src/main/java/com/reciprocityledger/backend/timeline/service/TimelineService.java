package com.reciprocityledger.backend.timeline.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.auth.AuthSessionService;
import com.reciprocityledger.backend.timeline.dto.request.TimelineQueryRequest;
import com.reciprocityledger.backend.timeline.dto.response.TimelineItemResponse;
import com.reciprocityledger.backend.timeline.mapper.TimelineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final AuthSessionService authSessionService;
    private final TimelineMapper timelineMapper;

    public PageResponse<TimelineItemResponse> query(TimelineQueryRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        int pageNo = safePageNo(request.getPageNo());
        int pageSize = safePageSize(request.getPageSize());
        String recordType = normalizeNullable(request.getRecordType());
        String relation = normalizeNullable(request.getRelation());
        String eventTypeCode = normalizeNullable(request.getEventTypeCode());
        String keyword = normalizeNullable(request.getKeyword());
        long total = timelineMapper.countPage(userId, recordType, request.getContactId(), relation, eventTypeCode, request.getStartDate(), request.getEndDate(), keyword);
        List<TimelineItemResponse> list = timelineMapper.selectPage(userId, recordType, request.getContactId(), relation, eventTypeCode, request.getStartDate(), request.getEndDate(), keyword, (pageNo - 1) * pageSize, pageSize);
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    private String normalizeNullable(String value) {
        String normalizedValue = StrUtil.trim(value);
        return StrUtil.isBlank(normalizedValue) ? null : normalizedValue;
    }

    private int safePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
