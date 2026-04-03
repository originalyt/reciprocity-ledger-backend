package com.reciprocityledger.backend.reciprocity.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReciprocityPageRequest extends PageRequest {

    private String contactId;
    private String eventTypeId;
    private String reciprocityStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}
