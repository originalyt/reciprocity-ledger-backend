package com.reciprocityledger.backend.stats.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewResponse {

    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private BigDecimal netAmount;
    private Long receiveCount;
    private Long sendCount;
}
