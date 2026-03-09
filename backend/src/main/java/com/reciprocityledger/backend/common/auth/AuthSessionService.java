package com.reciprocityledger.backend.common.auth;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthSessionService {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 2 * 60 * 60;

    private final Map<String, AccessTokenSession> sessions = new ConcurrentHashMap<>();

    public AuthenticatedSession createSession(Long userId) {
        String accessToken = IdUtil.fastSimpleUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRES_IN_SECONDS);
        sessions.put(accessToken, new AccessTokenSession(userId, expiresAt));
        return new AuthenticatedSession(accessToken, expiresAt, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    public Long requireUserId(String accessToken) {
        if (StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "accessToken不能为空");
        }
        AccessTokenSession session = sessions.get(accessToken);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态无效");
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessions.remove(accessToken);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "登录状态已过期");
        }
        return session.getUserId();
    }

    public void invalidate(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            sessions.remove(accessToken);
        }
    }
}
