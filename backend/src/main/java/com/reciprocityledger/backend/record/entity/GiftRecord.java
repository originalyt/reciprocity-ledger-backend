package com.reciprocityledger.backend.record.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人情记录实体。
 * 映射数据库表：rl_gift_record。
 */
@Data
public class GiftRecord {

    /**
     * 主键，人情记录唯一标识。
     */
    private String id;

    /**
     * 联系人主键，用于确定往来对象。
     */
    private String contactId;

    /**
     * 事件主键，用于确定记录发生的具体事件背景。
     */
    private String eventId;

    /**
     * 收送方向，统一从本人视角定义。
     */
    private String direction;

    /**
     * 金额，只记录金额型人情。
     */
    private BigDecimal amount;

    /**
     * 记录日期，用于时间线和统计口径计算。
     */
    private LocalDate recordDate;

    /**
     * 备注，用于补充这笔记录的特殊说明。
     */
    private String remark;

    /**
     * 闭环状态，用于快速展示当前记录是否已经形成回礼闭环。
     */
    private String reciprocityStatus;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 所属用户ID，多租户数据隔离。
     */
    private String userId;
}
