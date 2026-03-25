package com.reciprocityledger.backend.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EventSaveRequest {

    @NotBlank(message = "eventName不能为空")
    private String eventName;
    @NotBlank(message = "eventTypeId不能为空")
    private String eventTypeId;
    @NotBlank(message = "eventOwnerType不能为空")
    private String eventOwnerType;
    private String ownerContactId;
    @NotNull(message = "eventDate不能为空")
    private LocalDate eventDate;
    private String remark;
}
