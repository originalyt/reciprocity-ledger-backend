package com.reciprocityledger.backend.timeline;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.timeline.dto.request.TimelineQueryRequest;
import com.reciprocityledger.backend.timeline.dto.response.TimelineItemResponse;
import com.reciprocityledger.backend.timeline.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @PostMapping("/query")
    public ApiResponse<PageResponse<TimelineItemResponse>> query(@Valid @RequestBody TimelineQueryRequest request) {
        return ApiResponse.success(timelineService.query(request));
    }
}
