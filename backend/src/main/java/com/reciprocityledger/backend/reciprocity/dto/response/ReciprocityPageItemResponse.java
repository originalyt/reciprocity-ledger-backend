package com.reciprocityledger.backend.reciprocity.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReciprocityPageItemResponse {

    private String reciprocityMatchId;
    private String contactId;
    private String contactName;
    private String eventTypeId;
    private String eventTypeName;
    private String reciprocityStatus;
    private String matchType;
    private List<ReciprocityRecordItem> records;

    @JsonIgnore
    private String eventId;
    @JsonIgnore
    private String eventName;
    @JsonIgnore
    private String eventOwnerType;
    @JsonIgnore
    private BigDecimal amount;
    @JsonIgnore
    private LocalDate recordDate;

    @Data
    public static class ReciprocityRecordItem {
        private String eventId;
        private String eventName;
        private String eventOwnerType;
        private BigDecimal amount;
        private LocalDate recordDate;
    }
}
