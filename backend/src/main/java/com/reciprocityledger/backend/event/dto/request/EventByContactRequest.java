package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventByContactRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
}
