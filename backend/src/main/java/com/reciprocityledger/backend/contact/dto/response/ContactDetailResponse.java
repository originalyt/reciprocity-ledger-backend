package com.reciprocityledger.backend.contact.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContactDetailResponse {

    private String contactId;
    private String contactName;
    private String aliasName;
    private String salutation;
    private String mobile;
    private String relationType;
    private String remark;
    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private BigDecimal netAmount;
    private LocalDate lastRecordDate;
    private Long unclosedReciprocityCount;
}
