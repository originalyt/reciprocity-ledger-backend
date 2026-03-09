package com.reciprocityledger.backend.auth.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.SecureUtil;
import com.reciprocityledger.backend.auth.dto.request.LoginRequest;
import com.reciprocityledger.backend.auth.dto.request.LogoutRequest;
import com.reciprocityledger.backend.auth.dto.request.RefreshTokenRequest;
import com.reciprocityledger.backend.auth.dto.request.RegisterRequest;
import com.reciprocityledger.backend.auth.dto.response.LoginResponse;
import com.reciprocityledger.backend.auth.dto.response.LogoutResponse;
import com.reciprocityledger.backend.auth.dto.response.RefreshTokenResponse;
import com.reciprocityledger.backend.auth.dto.response.UserInfoResponse;
import com.reciprocityledger.backend.auth.entity.AppUser;
import com.reciprocityledger.backend.auth.entity.UserRefreshToken;
import com.reciprocityledger.backend.auth.mapper.AppUserMapper;
import com.reciprocityledger.backend.auth.mapper.UserRefreshTokenMapper;
import com.reciprocityledger.backend.common.auth.AuthSessionService;
import com.reciprocityledger.backend.common.auth.AuthenticatedSession;
import com.reciprocityledger.backend.common.enums.UserStatusEnum;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务。
 * 当前实现使用邮箱注册和邮箱密码登录，手机号保留为后续绑定能力。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * refreshToken 默认有效期 30 天，便于移动端保持登录态。
     */
    private static final long REFRESH_TOKEN_EXPIRES_DAYS = 30L;

    private final AppUserMapper appUserMapper;
    private final UserRefreshTokenMapper userRefreshTokenMapper;
    private final AuthSessionService authSessionService;
    private final IdGenerator idGenerator;

    /**
     * 注册完成后直接返回登录态，减少客户端额外再登录一次的交互成本。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        validatePassword(request.getPassword());
        if (appUserMapper.selectByEmail(email) != null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "邮箱已注册");
        }

        LocalDateTime now = LocalDateTime.now();
        AppUser appUser = new AppUser();
        appUser.setId(idGenerator.nextId());
        appUser.setEmail(email);
        appUser.setEmailVerified(Boolean.FALSE);
        appUser.setPhone(null);
        appUser.setPhoneVerified(Boolean.FALSE);
        String passwordSalt = IdUtil.fastSimpleUUID();
        appUser.setPasswordSalt(passwordSalt);
        appUser.setPasswordHash(hashPassword(request.getPassword(), passwordSalt));
        appUser.setNickname(buildNickname(request.getNickname(), email));
        appUser.setStatus(UserStatusEnum.ACTIVE.name());
        appUser.setLastLoginAt(now);
        appUser.setCreatedBy(appUser.getId());
        appUser.setUpdatedBy(appUser.getId());
        appUserMapper.insert(appUser);

        return buildLoginResponse(appUser, request.getDeviceInfo(), now);
    }

    /**
     * 登录流程：按邮箱查用户、校验密码、更新最近登录时间、签发 accessToken 和 refreshToken。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AppUser appUser = appUserMapper.selectByEmail(email);
        if (appUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }
        if (!matchesPassword(request.getPassword(), appUser.getPasswordSalt(), appUser.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }
        if (UserStatusEnum.DISABLED.name().equals(appUser.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
        }

        LocalDateTime now = LocalDateTime.now();
        appUserMapper.updateLastLogin(appUser.getId(), now);
        appUser.setLastLoginAt(now);
        return buildLoginResponse(appUser, request.getDeviceInfo(), now);
    }

    /**
     * refreshToken 只负责换取新的 accessToken，不在这里轮换 refreshToken，先保持流程简单可控。
     */
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = StrUtil.trim(request.getRefreshToken());
        UserRefreshToken token = userRefreshTokenMapper.selectByToken(refreshToken);
        if (token == null || Boolean.TRUE.equals(token.getRevoked()) || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "refreshToken已失效");
        }
        AppUser appUser = appUserMapper.selectById(token.getUserId());
        if (appUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        if (UserStatusEnum.DISABLED.name().equals(appUser.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
        }
        AuthenticatedSession session = authSessionService.createSession(appUser.getId());
        return new RefreshTokenResponse(session.getAccessToken(), session.getExpiresIn());
    }

    /**
     * 退出登录时同时清掉内存 accessToken 和数据库 refreshToken，避免旧会话继续使用。
     */
    @Transactional(rollbackFor = Exception.class)
    public LogoutResponse logout(LogoutRequest request) {
        authSessionService.invalidate(StrUtil.trim(request.getAccessToken()));
        if (StrUtil.isNotBlank(request.getRefreshToken())) {
            userRefreshTokenMapper.revokeByToken(StrUtil.trim(request.getRefreshToken()));
        }
        return new LogoutResponse(Boolean.TRUE);
    }

    private LoginResponse buildLoginResponse(AppUser appUser, String deviceInfo, LocalDateTime now) {
        AuthenticatedSession session = authSessionService.createSession(appUser.getId());
        UserRefreshToken refreshToken = new UserRefreshToken();
        refreshToken.setId(idGenerator.nextId());
        refreshToken.setUserId(appUser.getId());
        refreshToken.setRefreshToken(IdUtil.fastSimpleUUID());
        refreshToken.setDeviceInfo(normalizeDeviceInfo(deviceInfo));
        refreshToken.setExpiresAt(now.plusDays(REFRESH_TOKEN_EXPIRES_DAYS));
        refreshToken.setRevoked(Boolean.FALSE);
        userRefreshTokenMapper.insert(refreshToken);
        return new LoginResponse(
                session.getAccessToken(),
                refreshToken.getRefreshToken(),
                session.getExpiresIn(),
                new UserInfoResponse(appUser.getId(), appUser.getEmail(), appUser.getPhone(), appUser.getNickname())
        );
    }

    private String buildNickname(String nickname, String email) {
        String normalizedNickname = StrUtil.trim(nickname);
        if (StrUtil.isNotBlank(normalizedNickname)) {
            return normalizedNickname.length() > 64 ? StrUtil.subPre(normalizedNickname, 64) : normalizedNickname;
        }
        return StrUtil.subBefore(email, "@", false);
    }

    private String normalizeDeviceInfo(String deviceInfo) {
        String normalizedValue = StrUtil.trim(deviceInfo);
        if (StrUtil.isBlank(normalizedValue)) {
            return null;
        }
        return normalizedValue.length() > 200 ? StrUtil.subPre(normalizedValue, 200) : normalizedValue;
    }

    /**
     * 登录邮箱统一先做格式校验，避免账号表出现脏数据。
     */
    private String normalizeEmail(String email) {
        String normalizedEmail = StrUtil.trim(email);
        if (!Validator.isEmail(normalizedEmail)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "邮箱格式不正确");
        }
        return StrUtil.toLowerCase(normalizedEmail);
    }

    private void validatePassword(String password) {
        String normalizedPassword = StrUtil.trim(password);
        if (StrUtil.isBlank(normalizedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "password不能为空");
        }
        if (normalizedPassword.length() < 8 || normalizedPassword.length() > 32) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "password长度需要在8到32之间");
        }
    }

    private String hashPassword(String password, String salt) {
        return SecureUtil.sha256(salt + ":" + password);
    }

    private boolean matchesPassword(String inputPassword, String salt, String passwordHash) {
        return StrUtil.equals(hashPassword(inputPassword, salt), passwordHash);
    }
}

