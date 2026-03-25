package com.reciprocityledger.backend.reciprocity.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReciprocityPageItemResponse {

    private String recordId;
    private String reciprocityMatchId;
    private String contactId;
    private String contactName;
    private String eventId;
    private String eventName;
    private String eventTypeCode;
    private String eventTypeName;
    private String direction;
    private BigDecimal amount;
    private LocalDate recordDate;
    private String reciprocityStatus;
    private String matchedRecordId;
    private BigDecimal matchedAmount;
    private LocalDate matchedRecordDate;
}
