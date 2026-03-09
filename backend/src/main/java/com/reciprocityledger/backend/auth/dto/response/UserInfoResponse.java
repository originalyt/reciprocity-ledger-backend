package com.reciprocityledger.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String phone;
    private String nickname;
}
