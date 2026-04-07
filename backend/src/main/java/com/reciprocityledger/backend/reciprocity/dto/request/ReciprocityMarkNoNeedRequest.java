package com.reciprocityledger.backend.reciprocity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReciprocityMarkNoNeedRequest {

    @NotBlank(message = "记录ID不能为空")
    private String recordId;
    private String reason;
}