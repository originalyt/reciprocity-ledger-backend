package com.reciprocityledger.backend.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthenticatedSession {

    private String accessToken;
    private LocalDateTime expiresAt;
    private Long expiresIn;
}
