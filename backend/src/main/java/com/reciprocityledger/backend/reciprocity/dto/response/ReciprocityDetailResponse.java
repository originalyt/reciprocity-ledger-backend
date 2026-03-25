package com.reciprocityledger.backend.reciprocity.dto.response;

import com.reciprocityledger.backend.record.dto.response.RecordDetailResponse;
import lombok.Data;

@Data
public class ReciprocityDetailResponse {

    private RecordDetailResponse recordInfo;
    private RecordDetailResponse matchedRecordInfo;
    private ReciprocityHistoryReferenceResponse historyReference;
    private ReciprocityMatchInfoResponse manualFlagInfo;
}
