package com.reciprocityledger.backend.event;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.event.dto.request.EventByContactRequest;
import com.reciprocityledger.backend.event.dto.request.EventDetailRequest;
import com.reciprocityledger.backend.event.dto.request.EventPageRequest;
import com.reciprocityledger.backend.event.dto.request.EventSaveRequest;
import com.reciprocityledger.backend.event.dto.request.EventUpdateRequest;
import com.reciprocityledger.backend.event.dto.response.EventByContactResponse;
import com.reciprocityledger.backend.event.dto.response.EventDetailResponse;
import com.reciprocityledger.backend.event.dto.response.EventPageItemResponse;
import com.reciprocityledger.backend.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/app/event/page")
    public ApiResponse<PageResponse<EventPageItemResponse>> page(@Valid @RequestBody EventPageRequest request) {
        return ApiResponse.success(eventService.page(request));
    }

    @PostMapping("/app/event/detail")
    public ApiResponse<EventDetailResponse> detail(@Valid @RequestBody EventDetailRequest request) {
        return ApiResponse.success(eventService.detail(request));
    }

    @PostMapping("/app/event/save")
    public ApiResponse<IdResponse> save(@Valid @RequestBody EventSaveRequest request) {
        return ApiResponse.success(eventService.save(request));
    }

    @PostMapping("/app/event/update")
    public ApiResponse<IdResponse> update(@Valid @RequestBody EventUpdateRequest request) {
        return ApiResponse.success(eventService.update(request));
    }

    @PostMapping("/app/event/by-contact")
    public ApiResponse<EventByContactResponse> byContact(@Valid @RequestBody EventByContactRequest request) {
        return ApiResponse.success(eventService.eventsByContact(request.getContactId()));
    }
}
