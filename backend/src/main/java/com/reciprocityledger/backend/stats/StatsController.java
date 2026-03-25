package com.reciprocityledger.backend.stats;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.stats.dto.request.StatsByContactRequest;
import com.reciprocityledger.backend.stats.dto.request.StatsByEventTypeRequest;
import com.reciprocityledger.backend.stats.dto.request.StatsOverviewRequest;
import com.reciprocityledger.backend.stats.dto.response.StatsByContactItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsByEventTypeItemResponse;
import com.reciprocityledger.backend.stats.dto.response.StatsOverviewResponse;
import com.reciprocityledger.backend.stats.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/app/stats/overview")
    public ApiResponse<StatsOverviewResponse> overview(@Valid @RequestBody StatsOverviewRequest request) {
        return ApiResponse.success(statsService.overview(request));
    }

    @PostMapping("/app/stats/by-contact")
    public ApiResponse<PageResponse<StatsByContactItemResponse>> byContact(@Valid @RequestBody StatsByContactRequest request) {
        return ApiResponse.success(statsService.byContact(request));
    }

    @PostMapping("/app/stats/by-event-type")
    public ApiResponse<ListResponse<StatsByEventTypeItemResponse>> byEventType(@Valid @RequestBody StatsByEventTypeRequest request) {
        return ApiResponse.success(statsService.byEventType(request));
    }
}
