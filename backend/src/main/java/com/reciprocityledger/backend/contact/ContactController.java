package com.reciprocityledger.backend.contact;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.contact.dto.request.ContactDetailRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactPageRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactSaveRequest;
import com.reciprocityledger.backend.contact.dto.request.ContactUpdateRequest;
import com.reciprocityledger.backend.contact.dto.response.ContactDetailResponse;
import com.reciprocityledger.backend.contact.dto.response.ContactPageItemResponse;
import com.reciprocityledger.backend.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/app/contact/page")
    public ApiResponse<PageResponse<ContactPageItemResponse>> page(@Valid @RequestBody ContactPageRequest request) {
        return ApiResponse.success(contactService.page(request));
    }

    @PostMapping("/app/contact/detail")
    public ApiResponse<ContactDetailResponse> detail(@Valid @RequestBody ContactDetailRequest request) {
        return ApiResponse.success(contactService.detail(request));
    }

    @PostMapping("/app/contact/save")
    public ApiResponse<IdResponse> save(@Valid @RequestBody ContactSaveRequest request) {
        return ApiResponse.success(contactService.save(request));
    }

    @PostMapping("/app/contact/update")
    public ApiResponse<IdResponse> update(@Valid @RequestBody ContactUpdateRequest request) {
        return ApiResponse.success(contactService.update(request));
    }
}
