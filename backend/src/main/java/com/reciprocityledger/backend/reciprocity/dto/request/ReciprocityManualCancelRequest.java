package com.reciprocityledger.backend.reciprocity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReciprocityManualCancelRequest {

    @NotBlank(message = "reciprocityMatchId不能为空")
    private String reciprocityMatchId;
    private String cancelReason;
}
