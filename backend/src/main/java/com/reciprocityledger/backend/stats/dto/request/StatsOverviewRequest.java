package com.reciprocityledger.backend.stats.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatsOverviewRequest {

    private LocalDate startDate;
    private LocalDate endDate;
}
