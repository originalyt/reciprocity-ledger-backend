package com.reciprocityledger.backend.event.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 未关联事件响应。
 */
@Data
public class UnlinkedEventVO {

    /**
     * 事件ID。
     */
    private String eventId;

    /**
     * 事件名称。
     */
    private String eventName;

    /**
     * 事件类型ID。
     */
    private String eventTypeId;

    /**
     * 事件类型名称。
     */
    private String eventTypeName;

    /**
     * 事件日期。
     */
    private LocalDate eventDate;

    /**
     * 联系人ID。
     */
    private String contactId;

    /**
     * 联系人名称。
     */
    private String contactName;

    /**
     * 送礼金额。
     */
    private BigDecimal sendAmount;
}
