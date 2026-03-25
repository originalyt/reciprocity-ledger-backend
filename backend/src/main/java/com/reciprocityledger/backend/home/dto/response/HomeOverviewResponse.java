package com.reciprocityledger.backend.home.dto.response;

import com.reciprocityledger.backend.event.dto.response.EventPageItemResponse;
import com.reciprocityledger.backend.record.dto.response.RecordPageItemResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HomeOverviewResponse {

    private BigDecimal receiveTotalAmount;
    private BigDecimal sendTotalAmount;
    private Long pendingReciprocityCount;
    private List<RecordPageItemResponse> recentRecordList;
    private List<EventPageItemResponse> recentEventList;
}
