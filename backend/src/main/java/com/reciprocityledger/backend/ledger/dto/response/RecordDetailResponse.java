package com.reciprocityledger.backend.ledger.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordDetailResponse {

    private Long recordId;
    private Long eventExchangeId;
    private Long contactId;
    private String contactName;
    private String relation;
    private String recordType;
    private String eventTypeCode;
    private String eventNote;
    private LocalDate occurredOn;
    private BigDecimal amount;
    private String remark;
    private String reciprocityStatus;
}
