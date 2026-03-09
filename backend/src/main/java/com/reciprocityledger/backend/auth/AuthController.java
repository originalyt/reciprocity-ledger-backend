package com.reciprocityledger.backend.auth;

import com.reciprocityledger.backend.auth.dto.request.LoginRequest;
import com.reciprocityledger.backend.auth.dto.request.LogoutRequest;
import com.reciprocityledger.backend.auth.dto.request.RefreshTokenRequest;
import com.reciprocityledger.backend.auth.dto.request.SendSmsCodeRequest;
import com.reciprocityledger.backend.auth.dto.response.LoginResponse;
import com.reciprocityledger.backend.auth.dto.response.LogoutResponse;
import com.reciprocityledger.backend.auth.dto.response.RefreshTokenResponse;
import com.reciprocityledger.backend.auth.dto.response.SendSmsCodeResponse;
import com.reciprocityledger.backend.auth.service.AuthService;
import com.reciprocityledger.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public ApiResponse<SendSmsCodeResponse> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        return ApiResponse.success(authService.sendSmsCode(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success(authService.logout(request));
    }
}
