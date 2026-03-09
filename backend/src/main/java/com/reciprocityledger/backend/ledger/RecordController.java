package com.reciprocityledger.backend.ledger;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.ledger.dto.request.CreateRecordRequest;
import com.reciprocityledger.backend.ledger.dto.request.RecordDetailRequest;
import com.reciprocityledger.backend.ledger.dto.request.UpdateRecordRequest;
import com.reciprocityledger.backend.ledger.dto.response.RecordDetailResponse;
import com.reciprocityledger.backend.ledger.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/create")
    public ApiResponse<RecordDetailResponse> create(@Valid @RequestBody CreateRecordRequest request) {
        return ApiResponse.success(recordService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<RecordDetailResponse> update(@Valid @RequestBody UpdateRecordRequest request) {
        return ApiResponse.success(recordService.update(request));
    }

    @PostMapping("/detail")
    public ApiResponse<RecordDetailResponse> detail(@Valid @RequestBody RecordDetailRequest request) {
        return ApiResponse.success(recordService.detail(request));
    }
}
