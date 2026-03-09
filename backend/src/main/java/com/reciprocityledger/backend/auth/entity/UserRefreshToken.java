package com.reciprocityledger.backend.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户刷新令牌实体。
 * 映射数据库表：user_refresh_token。
 */
@Data
public class UserRefreshToken {

    /**
     * 主键ID，对应表主键 user_refresh_token.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 关联的用户ID。
     */
    private Long userId;

    /**
     * refreshToken 字符串。
     */
    private String refreshToken;

    /**
     * 登录设备信息。
     */
    private String deviceInfo;

    /**
     * token 过期时间。
     */
    private LocalDateTime expiresAt;

    /**
     * 是否已撤销。
     */
    private Boolean revoked;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
