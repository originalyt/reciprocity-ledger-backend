package com.reciprocityledger.backend.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录验证码实体。
 * 映射数据库表：login_verification_code。
 */
@Data
public class LoginVerificationCode {

    /**
     * 主键ID，对应表主键 login_verification_code.id，由应用层生成，非自增。
     */
    private Long id;

    /**
     * 接收验证码的手机号。
     */
    private String phone;

    /**
     * 验证码内容。
     */
    private String verificationCode;

    /**
     * 验证码过期时间。
     */
    private LocalDateTime expiresAt;

    /**
     * 是否已使用。
     */
    private Boolean used;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
