package com.reciprocityledger.backend.ledger.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 礼账明细实体。
 * 映射数据库表：ledger_record。
 */
@Data
public class LedgerRecord {

    /**
     * 明细主键ID，对应表主键 ledger_record.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 所属用户ID。
     */
    private Long userId;

    /**
     * 关联事件聚合ID。
     */
    private Long eventExchangeId;

    /**
     * 记录类型，取值如 GIVE、RECEIVE。
     */
    private String recordType;

    /**
     * 礼金发生日期。
     */
    private LocalDate occurredOn;

    /**
     * 礼金金额。
     */
    private BigDecimal amount;

    /**
     * 备注说明。
     */
    private String remark;

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
