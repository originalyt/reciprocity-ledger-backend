package com.reciprocityledger.backend.contact.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 联系人实体。
 * 映射数据库表：contact。
 */
@Data
public class Contact {

    /**
     * 联系人主键ID，对应表主键 contact.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 所属用户ID。
     */
    private Long userId;

    /**
     * 联系人姓名。
     */
    private String name;

    /**
     * 与我的关系。
     */
    private String relation;

    /**
     * 联系人手机号。
     */
    private String phone;

    /**
     * 联系人备注。
     */
    private String note;

    /**
     * 最近往来日期。
     */
    private LocalDate lastInteractionOn;

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
