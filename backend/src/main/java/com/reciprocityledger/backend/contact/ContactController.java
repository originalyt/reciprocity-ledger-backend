package com.reciprocityledger.backend.contact;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.contact.dto.request.ContactDetailRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactQueryRequest;
import com.reciprocityledger.backend.contact.dto.request.CreateContactRequest;
import com.reciprocityledger.backend.contact.dto.request.UpdateContactRequest;
import com.reciprocityledger.backend.contact.dto.response.ContactDetailResponse;
import com.reciprocityledger.backend.contact.dto.response.ContactSummaryResponse;
import com.reciprocityledger.backend.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/query")
    public ApiResponse<PageResponse<ContactSummaryResponse>> query(@Valid @RequestBody ContactQueryRequest request) {
        return ApiResponse.success(contactService.query(request));
    }

    @PostMapping("/create")
    public ApiResponse<ContactDetailResponse> create(@Valid @RequestBody CreateContactRequest request) {
        return ApiResponse.success(contactService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<ContactDetailResponse> update(@Valid @RequestBody UpdateContactRequest request) {
        return ApiResponse.success(contactService.update(request));
    }

    @PostMapping("/detail")
    public ApiResponse<ContactDetailResponse> detail(@Valid @RequestBody ContactDetailRequest request) {
        return ApiResponse.success(contactService.detail(request));
    }
}
