package com.reciprocityledger.backend.event.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 关联事件响应。
 */
@Data
public class EventRelationVO {

    /**
     * 关联ID。
     */
    private String id;

    /**
     * 联系人事件ID。
     */
    private String contactEventId;

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
