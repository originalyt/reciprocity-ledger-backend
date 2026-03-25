package com.reciprocityledger.backend.event.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EventDetailResponse {

    private String eventId;
    private String eventName;
    private String eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private String eventOwnerType;
    private String ownerContactId;
    private String ownerContactName;
    private LocalDate eventDate;
    private String remark;
    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private Long recordCount;
    private Long contactCount;
}
