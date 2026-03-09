package com.reciprocityledger.backend.auth.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.auth.dto.request.LoginRequest;
import com.reciprocityledger.backend.auth.dto.request.LogoutRequest;
import com.reciprocityledger.backend.auth.dto.request.RefreshTokenRequest;
import com.reciprocityledger.backend.auth.dto.request.SendSmsCodeRequest;
import com.reciprocityledger.backend.auth.dto.response.LoginResponse;
import com.reciprocityledger.backend.auth.dto.response.LogoutResponse;
import com.reciprocityledger.backend.auth.dto.response.RefreshTokenResponse;
import com.reciprocityledger.backend.auth.dto.response.SendSmsCodeResponse;
import com.reciprocityledger.backend.auth.dto.response.UserInfoResponse;
import com.reciprocityledger.backend.auth.entity.AppUser;
import com.reciprocityledger.backend.auth.entity.LoginVerificationCode;
import com.reciprocityledger.backend.auth.entity.UserRefreshToken;
import com.reciprocityledger.backend.auth.mapper.AppUserMapper;
import com.reciprocityledger.backend.auth.mapper.LoginVerificationCodeMapper;
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
 * 当前实现先使用“内存 accessToken + 数据库 refreshToken”的轻量方案，优先打通 Phase 1 登录闭环。
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
    private final LoginVerificationCodeMapper loginVerificationCodeMapper;
    private final UserRefreshTokenMapper userRefreshTokenMapper;
    private final AuthSessionService authSessionService;
    private final IdGenerator idGenerator;

    /**
     * 发送验证码。
     * 当前阶段直接返回验证码结果，便于本地联调，后续可替换为真实短信网关。
     */
    @Transactional(rollbackFor = Exception.class)
    public SendSmsCodeResponse sendSmsCode(SendSmsCodeRequest request) {
        String phone = normalizePhone(request.getPhone());
        String code = RandomUtil.randomNumbers(6);
        LoginVerificationCode verificationCode = new LoginVerificationCode();
        verificationCode.setId(idGenerator.nextId());
        verificationCode.setPhone(phone);
        verificationCode.setVerificationCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(Boolean.FALSE);
        loginVerificationCodeMapper.insert(verificationCode);
        log.info("Generated sms code for phone={}", phone);
        return new SendSmsCodeResponse(phone, code, verificationCode.getExpiresAt());
    }

    /**
     * 登录流程：校验验证码、自动注册用户、刷新最近登录时间、签发 accessToken 和 refreshToken。
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        String phone = normalizePhone(request.getPhone());
        String smsCode = StrUtil.trim(request.getSmsCode());
        LoginVerificationCode verificationCode = loginVerificationCodeMapper.selectValidCode(phone, smsCode);
        if (verificationCode == null) {
            throw new BusinessException(ErrorCode.INVALID_SMS_CODE, "验证码错误或已过期");
        }
        loginVerificationCodeMapper.markUsed(verificationCode.getId());

        AppUser appUser = appUserMapper.selectByPhone(phone);
        if (appUser == null) {
            appUser = createUser(phone);
        }
        if (UserStatusEnum.DISABLED.name().equals(appUser.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用");
        }

        LocalDateTime now = LocalDateTime.now();
        appUserMapper.updateLastLogin(appUser.getId(), now);
        appUser.setLastLoginAt(now);

        // accessToken 放内存，响应快；refreshToken 落库，便于后续做失效控制和设备管理。
        AuthenticatedSession session = authSessionService.createSession(appUser.getId());
        UserRefreshToken refreshToken = new UserRefreshToken();
        refreshToken.setId(idGenerator.nextId());
        refreshToken.setUserId(appUser.getId());
        refreshToken.setRefreshToken(IdUtil.fastSimpleUUID());
        refreshToken.setDeviceInfo(normalizeDeviceInfo(request.getDeviceInfo()));
        refreshToken.setExpiresAt(now.plusDays(REFRESH_TOKEN_EXPIRES_DAYS));
        refreshToken.setRevoked(Boolean.FALSE);
        userRefreshTokenMapper.insert(refreshToken);

        return new LoginResponse(
                session.getAccessToken(),
                refreshToken.getRefreshToken(),
                session.getExpiresIn(),
                new UserInfoResponse(appUser.getId(), appUser.getPhone(), appUser.getNickname())
        );
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

    /**
     * 当前产品策略是首次验证码登录自动注册。
     */
    private AppUser createUser(String phone) {
        AppUser appUser = new AppUser();
        appUser.setId(idGenerator.nextId());
        appUser.setPhone(phone);
        appUser.setNickname("用户" + StrUtil.subSuf(phone, Math.max(0, phone.length() - 4)));
        appUser.setStatus(UserStatusEnum.ACTIVE.name());
        appUser.setLastLoginAt(LocalDateTime.now());
        appUserMapper.insert(appUser);
        return appUser;
    }

    private String normalizeDeviceInfo(String deviceInfo) {
        String normalizedValue = StrUtil.trim(deviceInfo);
        if (StrUtil.isBlank(normalizedValue)) {
            return null;
        }
        return normalizedValue.length() > 200 ? StrUtil.subPre(normalizedValue, 200) : normalizedValue;
    }

    /**
     * 登录手机号统一先做格式校验，避免后续验证码和用户表出现脏数据。
     */
    private String normalizePhone(String phone) {
        String normalizedPhone = StrUtil.trim(phone);
        if (!PhoneUtil.isMobile(normalizedPhone)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "手机号格式不正确");
        }
        return normalizedPhone;
    }
}
