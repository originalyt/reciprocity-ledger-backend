package com.reciprocityledger.backend.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecordDetailRequest {

    @NotBlank(message = "recordId不能为空")
    private String recordId;
}
