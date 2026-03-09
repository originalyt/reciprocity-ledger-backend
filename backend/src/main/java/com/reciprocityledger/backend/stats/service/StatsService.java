package com.reciprocityledger.backend.stats.service;

import com.reciprocityledger.backend.common.auth.AuthSessionService;
import com.reciprocityledger.backend.stats.dto.request.OverviewStatsRequest;
import com.reciprocityledger.backend.stats.dto.response.OverviewStatsResponse;
import com.reciprocityledger.backend.stats.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final AuthSessionService authSessionService;
    private final StatsMapper statsMapper;

    public OverviewStatsResponse overview(OverviewStatsRequest request) {
        Long userId = authSessionService.requireUserId(request.getAccessToken());
        BigDecimal totalGiveAmount = defaultZero(statsMapper.sumGiveAmount(userId, request.getStartDate(), request.getEndDate()));
        BigDecimal totalReceiveAmount = defaultZero(statsMapper.sumReceiveAmount(userId, request.getStartDate(), request.getEndDate()));
        return new OverviewStatsResponse(totalGiveAmount, totalReceiveAmount, totalReceiveAmount.subtract(totalGiveAmount));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
