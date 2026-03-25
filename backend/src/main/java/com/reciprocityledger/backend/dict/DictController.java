package com.reciprocityledger.backend.dict;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.common.api.ListResponse;
import com.reciprocityledger.backend.dict.dto.request.EventTypeListRequest;
import com.reciprocityledger.backend.dict.dto.request.RelationTypeListRequest;
import com.reciprocityledger.backend.dict.dto.response.DictItemResponse;
import com.reciprocityledger.backend.dict.service.DictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @PostMapping("/app/dict/event-type/list")
    public ApiResponse<ListResponse<DictItemResponse>> listEventTypes(@Valid @RequestBody EventTypeListRequest request) {
        return ApiResponse.success(dictService.listEventTypes(request));
    }

    @PostMapping("/app/dict/relation-type/list")
    public ApiResponse<ListResponse<DictItemResponse>> listRelationTypes(@Valid @RequestBody RelationTypeListRequest request) {
        return ApiResponse.success(dictService.listRelationTypes(request));
    }
}
