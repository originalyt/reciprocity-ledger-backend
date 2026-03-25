package com.reciprocityledger.backend.contact.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联系人实体。
 * 映射数据库表：rl_contact。
 */
@Data
public class Contact {

    /**
     * 主键，联系人唯一标识。
     */
    private String id;

    /**
     * 联系人姓名。
     */
    private String contactName;

    /**
     * 联系人别名。
     */
    private String aliasName;

    /**
     * 联系人称呼。
     */
    private String salutation;

    /**
     * 联系人手机号。
     */
    private String mobile;

    /**
     * 关系类型，用于联系人分类和筛选。
     */
    private String relationType;

    /**
     * 备注，用于记录联系人补充说明。
     */
    private String remark;

    /**
     * 状态，用于标识联系人是否可继续使用。
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
}
