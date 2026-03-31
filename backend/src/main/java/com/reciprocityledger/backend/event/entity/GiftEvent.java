package com.reciprocityledger.backend.event.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 事件实体。
 * 映射数据库表：rl_event。
 */
@Data
public class GiftEvent {

    /**
     * 主键，事件唯一标识。
     */
    private String id;

    /**
     * 事件名称。
     */
    private String eventName;

    /**
     * 事件类型主键。
     */
    private String eventTypeId;

    /**
     * 事件归属类型，SELF 表示本人事件，CONTACT 表示联系人事件。
     */
    private String eventOwnerType;

    /**
     * 当事件归属为联系人事件时，对应的联系人主键。
     */
    private String ownerContactId;

    /**
     * 事件日期。
     */
    private LocalDate eventDate;

    /**
     * 备注，用于补充事件背景信息。
     */
    private String remark;

    /**
     * 状态，控制事件是否允许继续用于录入。
     */
    private String status;

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
