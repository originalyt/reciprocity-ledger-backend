package com.reciprocityledger.backend.ledger.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateRecordRequest extends BaseAccessTokenRequest {

    @NotNull(message = "recordId不能为空")
    private Long recordId;

    @NotNull(message = "contactId不能为空")
    private Long contactId;

    @NotBlank(message = "recordType不能为空")
    private String recordType;

    @NotBlank(message = "eventTypeCode不能为空")
    private String eventTypeCode;

    private String eventNote;

    @NotNull(message = "occurredOn不能为空")
    private LocalDate occurredOn;

    @NotNull(message = "amount不能为空")
    private BigDecimal amount;

    private String remark;
}
