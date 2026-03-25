package com.reciprocityledger.backend.home.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HomeOverviewRequest {

    private LocalDate startDate;
    private LocalDate endDate;
}
