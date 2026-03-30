package com.reciprocityledger.backend.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordSaveWithEventRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;

    @NotBlank(message = "eventName不能为空")
    private String eventName;

    @NotBlank(message = "eventTypeId不能为空")
    private String eventTypeId;

    @NotBlank(message = "eventOwnerType不能为空")
    private String eventOwnerType;

    private String ownerContactId;

    @NotNull(message = "eventDate不能为空")
    private LocalDate eventDate;

    private String eventRemark;

    @NotBlank(message = "direction不能为空")
    private String direction;

    @NotNull(message = "amount不能为空")
    private BigDecimal amount;

    @NotNull(message = "recordDate不能为空")
    private LocalDate recordDate;

    private String recordRemark;
}
