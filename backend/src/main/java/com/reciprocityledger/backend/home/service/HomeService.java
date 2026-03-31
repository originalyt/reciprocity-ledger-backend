package com.reciprocityledger.backend.home.service;

import com.reciprocityledger.backend.event.service.EventService;
import com.reciprocityledger.backend.home.dto.request.HomeOverviewRequest;
import com.reciprocityledger.backend.home.dto.response.HomeOverviewResponse;
import com.reciprocityledger.backend.record.mapper.RecordMapper;
import com.reciprocityledger.backend.stats.dto.request.StatsOverviewRequest;
import com.reciprocityledger.backend.stats.dto.response.StatsOverviewResponse;
import com.reciprocityledger.backend.stats.service.StatsService;
import com.reciprocityledger.backend.user.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final StatsService statsService;
    private final RecordMapper recordMapper;
    private final EventService eventService;

    public HomeOverviewResponse overview(HomeOverviewRequest request) {
        StatsOverviewRequest statsRequest = new StatsOverviewRequest();
        statsRequest.setStartDate(request.getStartDate());
        statsRequest.setEndDate(request.getEndDate());
        StatsOverviewResponse statsOverview = statsService.overview(statsRequest);
        HomeOverviewResponse response = new HomeOverviewResponse();
        response.setReceiveTotalAmount(statsOverview.getReceiveTotalAmount());
        response.setSendTotalAmount(statsOverview.getSendTotalAmount());
        response.setPendingReciprocityCount(statsService.pendingReciprocityCount());
        String userId = UserContext.getUserId();
        response.setRecentRecordList(recordMapper.selectRecentByUserId(userId, 10));
        response.setRecentEventList(eventService.recentEventsByUserId(userId, 10));
        return response;
    }
}
