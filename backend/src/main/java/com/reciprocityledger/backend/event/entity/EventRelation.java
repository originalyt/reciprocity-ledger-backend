package com.reciprocityledger.backend.event.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件关联实体。
 * 映射数据库表：rl_event_relation。
 */
@Data
public class EventRelation {

    /**
     * 主键。
     */
    private String id;

    /**
     * 本人事件ID（SELF类型）。
     */
    private String selfEventId;

    /**
     * 联系人事件ID（CONTACT类型）。
     */
    private String contactEventId;

    /**
     * 所属用户ID。
     */
    private String userId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
