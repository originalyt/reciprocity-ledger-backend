package com.reciprocityledger.backend.stats.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StatsByContactItemResponse {

    private String contactId;
    private String contactName;
    private String aliasName;
    private String relationType;
    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private BigDecimal netAmount;
    private LocalDate lastRecordDate;
}
