package com.reciprocityledger.backend.reciprocity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReciprocityDetailRequest {

    @NotBlank(message = "recordId不能为空")
    private String recordId;
}
