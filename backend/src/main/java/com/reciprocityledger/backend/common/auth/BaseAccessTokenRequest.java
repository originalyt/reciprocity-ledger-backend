package com.reciprocityledger.backend.common.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaseAccessTokenRequest {

    @NotBlank(message = "accessToken不能为空")
    private String accessToken;
}
