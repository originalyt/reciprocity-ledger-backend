package com.reciprocityledger.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank(message = "email不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "password不能为空")
    private String password;
}
