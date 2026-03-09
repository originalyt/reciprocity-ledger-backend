package com.reciprocityledger.backend.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccessTokenSession {

    private Long userId;
    private LocalDateTime expiresAt;
}
