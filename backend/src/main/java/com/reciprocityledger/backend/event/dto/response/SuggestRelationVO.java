package com.reciprocityledger.backend.event.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 可关联事件建议响应。
 */
@Data
public class SuggestRelationVO {

    /**
     * 事件ID。
     */
    private String eventId;

    /**
     * 事件名称。
     */
    private String eventName;

    /**
     * 事件日期。
     */
    private LocalDate eventDate;

    /**
     * 联系人名称。
     */
    private String contactName;

    /**
     * 送礼金额。
     */
    private BigDecimal sendAmount;
}
