package com.reciprocityledger.backend.health;

import com.reciprocityledger.backend.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class HealthController {

    @PostMapping("/health")
    public ApiResponse<Object> health() {
        return ApiResponse.success(Collections.singletonMap("status", "ok"));
    }
}
