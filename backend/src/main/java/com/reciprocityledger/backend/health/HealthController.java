package com.reciprocityledger.backend.health;

import com.reciprocityledger.backend.common.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("service", "reciprocity-ledger-backend");
        data.put("status", "UP");
        return ApiResponse.success(data);
    }
}
