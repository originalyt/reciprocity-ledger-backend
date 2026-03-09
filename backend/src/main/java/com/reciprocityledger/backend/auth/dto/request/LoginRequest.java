package com.reciprocityledger.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "phone不能为空")
    private String phone;

    @NotBlank(message = "smsCode不能为空")
    private String smsCode;

    private String clientRequestId;

    private String deviceInfo;
}
