package com.reciprocityledger.backend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {

    private String userId;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String token;
}
