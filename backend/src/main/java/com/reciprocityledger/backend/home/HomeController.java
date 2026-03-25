package com.reciprocityledger.backend.home;

import com.reciprocityledger.backend.common.api.ApiResponse;
import com.reciprocityledger.backend.home.dto.request.HomeOverviewRequest;
import com.reciprocityledger.backend.home.dto.response.HomeOverviewResponse;
import com.reciprocityledger.backend.home.service.HomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @PostMapping("/app/home/overview")
    public ApiResponse<HomeOverviewResponse> overview(@Valid @RequestBody HomeOverviewRequest request) {
        return ApiResponse.success(homeService.overview(request));
    }
}
