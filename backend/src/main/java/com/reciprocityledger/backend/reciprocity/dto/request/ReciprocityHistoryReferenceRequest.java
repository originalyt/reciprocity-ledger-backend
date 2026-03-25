package com.reciprocityledger.backend.reciprocity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReciprocityHistoryReferenceRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
    @NotBlank(message = "eventTypeId不能为空")
    private String eventTypeId;
}
