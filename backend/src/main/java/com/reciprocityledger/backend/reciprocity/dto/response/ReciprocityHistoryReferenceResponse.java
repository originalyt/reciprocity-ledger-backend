package com.reciprocityledger.backend.reciprocity.dto.response;

import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReciprocityHistoryReferenceResponse {

    private BigDecimal sameTypeReceiveAmount;
    private BigDecimal sameTypeSendAmount;
    private Long unclosedRecordCount;
    private RecordPageItemResponse lastSameTypeRecord;
}
