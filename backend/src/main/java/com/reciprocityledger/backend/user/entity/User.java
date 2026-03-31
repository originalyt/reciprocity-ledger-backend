package com.reciprocityledger.backend.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体。
 * 映射数据库表：rl_user。
 */
@Data
public class User {

    /**
     * 主键，用户唯一标识。
     */
    private String id;

    /**
     * 邮箱，用于登录。
     */
    private String email;

    /**
     * 密码，加密存储。
     */
    private String password;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像URL。
     */
    private String avatarUrl;

    /**
     * 状态，NORMAL表示正常，DISABLED表示停用。
     */
    private String status;

    /**
     * 最后登录时间。
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
