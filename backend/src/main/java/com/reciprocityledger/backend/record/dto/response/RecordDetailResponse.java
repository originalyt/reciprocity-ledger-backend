package com.reciprocityledger.backend.record.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecordDetailResponse {

    private String recordId;
    private String contactId;
    private String contactName;
    private String aliasName;
    private String salutation;
    private String eventId;
    private String eventName;
    private String eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private String eventOwnerType;
    private String ownerContactId;
    private String ownerContactName;
    private String direction;
    private BigDecimal amount;
    private LocalDate recordDate;
    private String remark;
    private String reciprocityStatus;
}
