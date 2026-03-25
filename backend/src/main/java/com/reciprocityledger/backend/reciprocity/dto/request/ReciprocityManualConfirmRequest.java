package com.reciprocityledger.backend.reciprocity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReciprocityManualConfirmRequest {

    @NotBlank(message = "sourceRecordId不能为空")
    private String sourceRecordId;
    @NotBlank(message = "targetRecordId不能为空")
    private String targetRecordId;
    private String remark;
}
