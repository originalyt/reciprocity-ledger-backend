package com.reciprocityledger.backend.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordSaveRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
    @NotBlank(message = "eventId不能为空")
    private String eventId;
    @NotBlank(message = "direction不能为空")
    private String direction;
    @NotNull(message = "amount不能为空")
    private BigDecimal amount;
    @NotNull(message = "recordDate不能为空")
    private LocalDate recordDate;
    private String remark;
}
