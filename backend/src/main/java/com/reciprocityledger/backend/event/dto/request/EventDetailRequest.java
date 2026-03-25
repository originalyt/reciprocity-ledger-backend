package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventDetailRequest {

    @NotBlank(message = "eventId不能为空")
    private String eventId;
}
