package com.reciprocityledger.backend.event.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EventPageItemResponse {

    private String eventId;
    private String eventName;
    private String eventTypeId;
    private String eventTypeCode;
    private String eventTypeName;
    private String eventOwnerType;
    private String ownerContactId;
    private String ownerContactName;
    private LocalDate eventDate;
    private Long recordCount;
}
