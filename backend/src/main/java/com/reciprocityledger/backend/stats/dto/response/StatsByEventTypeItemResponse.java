package com.reciprocityledger.backend.stats.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsByEventTypeItemResponse {

    private String eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private BigDecimal netAmount;
    private Long recordCount;
}
