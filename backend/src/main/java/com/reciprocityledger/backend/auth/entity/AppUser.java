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
     * 登录邮箱，唯一。
     */
    private String email;

    /**
     * 邮箱是否已验证。
     */
    private Boolean emailVerified;

    /**
     * 绑定手机号，唯一，可为空。
     */
    private String phone;

    /**
     * 手机号是否已验证。
     */
    private Boolean phoneVerified;

    /**
     * 密码哈希。
     */
    private String passwordHash;

    /**
     * 密码盐值。
     */
    private String passwordSalt;

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
