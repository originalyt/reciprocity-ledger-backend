package com.reciprocityledger.backend.stats;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.stats.dto.request.OverviewStatsRequest;
import com.reciprocityledger.backend.stats.dto.response.OverviewStatsResponse;
import com.reciprocityledger.backend.stats.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/overview")
    public ApiResponse<OverviewStatsResponse> overview(@Valid @RequestBody OverviewStatsRequest request) {
        return ApiResponse.success(statsService.overview(request));
    }
}
