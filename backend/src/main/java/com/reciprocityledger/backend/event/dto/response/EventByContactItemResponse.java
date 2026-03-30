package com.reciprocityledger.backend.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventByContactItemResponse {

    private String eventId;
    private String eventName;
    private String eventTypeName;
    private String eventTypeCode;
    private String eventOwnerType;
    private String ownerContactId;
    private String ownerContactName;
    private LocalDate eventDate;
}
