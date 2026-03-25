package com.reciprocityledger.backend.stats.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatsByContactRequest extends PageRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String relationType;
    private String keyword;
}
