package com.reciprocityledger.backend.user.controller;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.user.dto.request.UserLoginRequest;
import com.reciprocityledger.backend.user.dto.request.UserRegisterRequest;
import com.reciprocityledger.backend.user.dto.response.UserLoginResponse;
import com.reciprocityledger.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/app/user/register")
    public ApiResponse<UserLoginResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @PostMapping("/app/user/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}
