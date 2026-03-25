package com.reciprocityledger.backend.record.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecordPageRequest extends PageRequest {

    private String contactId;
    private String eventId;
    private String direction;
    private String eventTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
