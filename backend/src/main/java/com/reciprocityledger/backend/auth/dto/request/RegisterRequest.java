package com.reciprocityledger.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "email不能为空")
    private String email;

    @NotBlank(message = "password不能为空")
    private String password;

    private String nickname;

    private String deviceInfo;
}
