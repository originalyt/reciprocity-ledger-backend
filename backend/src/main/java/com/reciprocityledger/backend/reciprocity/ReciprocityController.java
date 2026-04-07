package com.reciprocityledger.backend.reciprocity;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.api.PageResponse;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityDetailRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityHistoryReferenceRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityManualCancelRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityManualConfirmRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityMarkNoNeedRequest;
import com.reciprocityledger.backend.reciprocity.dto.request.ReciprocityPageRequest;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityDetailResponse;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityHistoryReferenceResponse;
import com.reciprocityledger.backend.reciprocity.dto.response.ReciprocityPageItemResponse;
import com.reciprocityledger.backend.reciprocity.service.ReciprocityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReciprocityController {

    private final ReciprocityService reciprocityService;

    @PostMapping("/app/reciprocity/page")
    public ApiResponse<PageResponse<ReciprocityPageItemResponse>> page(@Valid @RequestBody ReciprocityPageRequest request) {
        return ApiResponse.success(reciprocityService.page(request));
    }

    @PostMapping("/app/reciprocity/detail")
    public ApiResponse<ReciprocityDetailResponse> detail(@Valid @RequestBody ReciprocityDetailRequest request) {
        return ApiResponse.success(reciprocityService.detail(request));
    }

    @PostMapping("/app/reciprocity/manual-confirm")
    public ApiResponse<IdResponse> manualConfirm(@Valid @RequestBody ReciprocityManualConfirmRequest request) {
        return ApiResponse.success(reciprocityService.manualConfirm(request));
    }

    @PostMapping("/app/reciprocity/manual-cancel")
    public ApiResponse<IdResponse> manualCancel(@Valid @RequestBody ReciprocityManualCancelRequest request) {
        return ApiResponse.success(reciprocityService.manualCancel(request));
    }

    @PostMapping("/app/reciprocity/history-reference")
    public ApiResponse<ReciprocityHistoryReferenceResponse> historyReference(@Valid @RequestBody ReciprocityHistoryReferenceRequest request) {
        return ApiResponse.success(reciprocityService.historyReference(request));
    }

    @PostMapping("/app/reciprocity/mark-no-need")
    public ApiResponse<IdResponse> markNoNeed(@Valid @RequestBody ReciprocityMarkNoNeedRequest request) {
        return ApiResponse.success(reciprocityService.markNoNeed(request));
    }
}
