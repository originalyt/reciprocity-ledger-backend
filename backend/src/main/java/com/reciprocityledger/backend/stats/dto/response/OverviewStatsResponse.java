package com.reciprocityledger.backend.stats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStatsResponse {

    private BigDecimal totalGiveAmount;
    private BigDecimal totalReceiveAmount;
    private BigDecimal netAmount;
}
