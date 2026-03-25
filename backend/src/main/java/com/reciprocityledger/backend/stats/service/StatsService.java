package com.reciprocityledger.backend.stats.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.common.util.PageUtils;
import com.reciprocityledger.backend.stats.dto.request.StatsByContactRequest;
import com.reciprocityledger.backend.stats.dto.request.StatsByEventTypeRequest;
import com.reciprocityledger.backend.stats.dto.request.StatsOverviewRequest;
import com.reciprocityledger.backend.stats.dto.response.StatsByContactItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsByEventTypeItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsOverviewResponse;
import com.reciprocityledger.backend.stats.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsOverviewResponse overview(StatsOverviewRequest request) {
        StatsOverviewResponse response = statsMapper.selectOverview(request.getStartDate(), request.getEndDate());
        if (response == null) {
            response = new StatsOverviewResponse();
        }
        if (response.getReceiveTotalAmount() == null) {
            response.setReceiveTotalAmount(BigDecimal.ZERO);
        }
        if (response.getSendTotalAmount() == null) {
            response.setSendTotalAmount(BigDecimal.ZERO);
        }
        if (response.getNetAmount() == null) {
            response.setNetAmount(BigDecimal.ZERO);
        }
        if (response.getReceiveCount() == null) {
            response.setReceiveCount(0L);
        }
        if (response.getSendCount() == null) {
            response.setSendCount(0L);
        }
        return response;
    }

    public PageResponse<StatsByContactItemResponse> byContact(StatsByContactRequest request) {
        int pageNo = PageUtils.safePageNo(request.getPageNo());
        int pageSize = PageUtils.safePageSize(request.getPageSize());
        String relationType = normalizeNullable(request.getRelationType());
        String keyword = normalizeNullable(request.getKeyword());
        List<StatsByContactItemResponse> list = statsMapper.selectByContact(request.getStartDate(), request.getEndDate(), relationType, keyword, PageUtils.offset(pageNo, pageSize), pageSize);
        long total = statsMapper.countByContact(request.getStartDate(), request.getEndDate(), relationType, keyword);
        return PageResponse.of(list, pageNo, pageSize, total);
    }

    public ListResponse<StatsByEventTypeItemResponse> byEventType(StatsByEventTypeRequest request) {
        return ListResponse.of(statsMapper.selectByEventType(request.getStartDate(), request.getEndDate()));
    }

    public Long pendingReciprocityCount() {
        Long value = statsMapper.countPendingReciprocity();
        return value == null ? 0L : value;
    }

    private String normalizeNullable(String value) {
        String normalized = StrUtil.trim(value);
        return StrUtil.isBlank(normalized) ? null : normalized;
    }
}


