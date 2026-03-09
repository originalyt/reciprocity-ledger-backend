package com.reciprocityledger.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsCodeResponse {

    private String phone;
    private String smsCode;
    private LocalDateTime expiresAt;
}
