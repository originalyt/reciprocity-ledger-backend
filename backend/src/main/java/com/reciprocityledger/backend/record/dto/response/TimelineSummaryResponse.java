package com.reciprocityledger.backend.record.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TimelineSummaryResponse {

    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private BigDecimal netAmount;
    private Long recordCount;
}
