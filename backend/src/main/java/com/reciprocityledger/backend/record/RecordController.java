package com.reciprocityledger.backend.record;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.record.dto.request.ContactTimelineRequest;
import com.reciprocityledger.backend.record.dto.request.RecordDetailRequest;
import com.reciprocityledger.backend.record.dto.request.RecordPageRequest;
import com.reciprocityledger.backend.record.dto.request.RecordQuickSaveContactRequest;
import com.reciprocityledger.backend.record.dto.request.RecordSaveRequest;
import com.reciprocityledger.backend.record.dto.request.RecordSaveSimpleRequest;
import com.reciprocityledger.backend.record.dto.request.RecordSaveWithEventRequest;
import com.reciprocityledger.backend.record.dto.request.RecordUpdateRequest;
import com.reciprocityledger.backend.record.dto.request.SelfTimelineRequest;
import com.reciprocityledger.backend.record.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import com.reciprocityledger.backend.record.dto.response.RecordQuickSaveContactResponse;
import com.reciprocityledger.backend.record.dto.response.TimelineResponse;
import com.reciprocityledger.backend.record.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/app/record/page")
    public ApiResponse<PageResponse<RecordPageItemResponse>> page(@Valid @RequestBody RecordPageRequest request) {
        return ApiResponse.success(recordService.page(request));
    }

    @PostMapping("/app/record/detail")
    public ApiResponse<RecordDetailResponse> detail(@Valid @RequestBody RecordDetailRequest request) {
        return ApiResponse.success(recordService.detail(request));
    }

    @PostMapping("/app/record/save")
    public ApiResponse<IdResponse> save(@Valid @RequestBody RecordSaveRequest request) {
        return ApiResponse.success(recordService.save(request));
    }

    @PostMapping("/app/record/save-with-event")
    public ApiResponse<IdResponse> saveWithEvent(@Valid @RequestBody RecordSaveWithEventRequest request) {
        return ApiResponse.success(recordService.saveWithEvent(request));
    }

    @PostMapping("/app/record/save-simple")
    public ApiResponse<IdResponse> saveSimple(@Valid @RequestBody RecordSaveSimpleRequest request) {
        return ApiResponse.success(recordService.saveSimple(request));
    }

    @PostMapping("/app/record/update")
    public ApiResponse<IdResponse> update(@Valid @RequestBody RecordUpdateRequest request) {
        return ApiResponse.success(recordService.update(request));
    }

    @PostMapping("/app/record/quick-save-contact")
    public ApiResponse<RecordQuickSaveContactResponse> quickSaveContact(@Valid @RequestBody RecordQuickSaveContactRequest request) {
        return ApiResponse.success(recordService.quickSaveContact(request));
    }

    @PostMapping("/app/record/self-timeline")
    public ApiResponse<TimelineResponse> selfTimeline(@Valid @RequestBody SelfTimelineRequest request) {
        return ApiResponse.success(recordService.selfTimeline(request));
    }

    @PostMapping("/app/record/contact-timeline")
    public ApiResponse<TimelineResponse> contactTimeline(@Valid @RequestBody ContactTimelineRequest request) {
        return ApiResponse.success(recordService.contactTimeline(request));
    }
}
