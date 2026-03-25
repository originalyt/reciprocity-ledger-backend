package com.reciprocityledger.backend.event.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventPageRequest extends PageRequest {

    private String keyword;
    private String eventTypeCode;
    private String eventOwnerType;
    private String ownerContactId;
    private LocalDate startDate;
    private LocalDate endDate;
}
