package com.reciprocityledger.backend.record.dto.response;

import com.reciprocityledger.backend.common.api.PageResponse;
import lombok.Data;

@Data
public class TimelineResponse {

    private TimelineSummaryResponse summaryInfo;
    private PageResponse<RecordPageItemResponse> pageResult;
}
