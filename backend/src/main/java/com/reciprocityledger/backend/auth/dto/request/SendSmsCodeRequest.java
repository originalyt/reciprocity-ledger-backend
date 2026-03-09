package com.reciprocityledger.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendSmsCodeRequest {

    @NotBlank(message = "phone不能为空")
    private String phone;
}
