package com.reciprocityledger.backend.reciprocity.dto.response;

import lombok.Data;

@Data
public class ReciprocityMatchInfoResponse {

    private String reciprocityMatchId;
    private String matchType;
    private String matchStatus;
    private String cancelReason;
    private String remark;
}
