package com.reciprocityledger.backend.stats.dto.request;

import com.reciprocityledger.backend.common.auth.BaseAccessTokenRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class OverviewStatsRequest extends BaseAccessTokenRequest {

    private LocalDate startDate;
    private LocalDate endDate;
}
