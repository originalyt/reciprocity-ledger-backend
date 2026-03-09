package com.reciprocityledger.backend.contact.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ContactSummaryResponse {

    private Long id;
    private String name;
    private String relation;
    private String phone;
    private LocalDate lastInteractionOn;
}
