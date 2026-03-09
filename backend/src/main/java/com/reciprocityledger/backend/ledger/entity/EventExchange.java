package com.reciprocityledger.backend.ledger.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 事件往来聚合实体。
 * 映射数据库表：event_exchange。
 */
@Data
public class EventExchange {

    /**
     * 聚合主键ID，对应表主键 event_exchange.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 所属用户ID。
     */
    private Long userId;

    /**
     * 关联联系人ID。
     */
    private Long contactId;

    /**
     * 事件类型编码。
     */
    private String eventTypeCode;

    /**
     * 事件说明，空串也参与唯一键。
     */
    private String eventNote;

    /**
     * 随礼记录ID。
     */
    private Long giveRecordId;

    /**
     * 收礼记录ID。
     */
    private Long receiveRecordId;

    /**
     * 随礼金额快照。
     */
    private BigDecimal giveAmount;

    /**
     * 收礼金额快照。
     */
    private BigDecimal receiveAmount;

    /**
     * 净额快照，通常为收礼金额减随礼金额。
     */
    private BigDecimal netAmount;

    /**
     * 最近发生日期。
     */
    private LocalDate latestOccurredOn;

    /**
     * 回礼状态，取值如 MUTUAL、WAIT_OTHER、WAIT_ME。
     */
    private String reciprocityStatus;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID。
     */
    private Long createdBy;

    /**
     * 更新人ID。
     */
    private Long updatedBy;
}
