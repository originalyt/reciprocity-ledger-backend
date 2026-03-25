package com.reciprocityledger.backend.record.dto.request;

import com.reciprocityledger.backend.common.api.PageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContactTimelineRequest extends PageRequest {

    @NotBlank(message = "contactId不能为空")
    private String contactId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String direction;
    private String eventTypeCode;
}
