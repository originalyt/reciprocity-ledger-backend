package com.reciprocityledger.backend.user.interceptor;

import com.reciprocityledger.backend.user.context.UserContext;
import com.reciprocityledger.backend.user.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/app/user/login",
            "/app/user/register"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = request.getRequestURI();

        if (shouldExclude(requestPath)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"401\",\"message\":\"未登录或token无效\"}");
            return false;
        }

        token = token.substring(7);

        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"401\",\"message\":\"Token已过期或无效\"}");
            return false;
        }

        String userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getEmailFromToken(token);

        UserContext.setUserId(userId);
        UserContext.setEmail(email);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean shouldExclude(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::endsWith);
    }
}
