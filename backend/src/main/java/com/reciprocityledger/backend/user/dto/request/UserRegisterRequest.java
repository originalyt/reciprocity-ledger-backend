package com.reciprocityledger.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "email不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "password不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    @Size(max = 32, message = "昵称最多32个字符")
    private String nickname;
}
