package com.reciprocityledger.backend.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用用户实体。
 * 映射数据库表：app_user。
 */
@Data
public class AppUser {

    /**
     * 用户主键ID，对应表主键 app_user.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 登录手机号。
     */
    private String phone;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户状态，取值如 ACTIVE、DISABLED。
     */
    private String status;

    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;

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
