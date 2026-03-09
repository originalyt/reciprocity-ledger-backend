package com.reciprocityledger.backend.ledger.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EventExchangeSnapshot {

    private Long giveRecordId;
    private Long receiveRecordId;
    private BigDecimal giveAmount;
    private BigDecimal receiveAmount;
    private LocalDate latestOccurredOn;
    private String reciprocityStatus;
}
