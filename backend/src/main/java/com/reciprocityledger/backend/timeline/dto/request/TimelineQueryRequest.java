package com.reciprocityledger.backend.timeline.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimelineQueryRequest extends BaseAccessTokenRequest {

    private String recordType;
    private Long contactId;
    private String relation;
    private String eventTypeCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
