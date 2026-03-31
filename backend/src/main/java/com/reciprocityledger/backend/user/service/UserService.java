package com.reciprocityledger.backend.user.service;

import cn.hutool.core.util.StrUtil;
import com.reciprocityledger.backend.common.api.IdResponse;
import com.reciprocityledger.backend.common.exception.BusinessException;
import com.reciprocityledger.backend.common.exception.ErrorCode;
import com.reciprocityledger.backend.common.id.IdGenerator;
import com.reciprocityledger.backend.user.dto.request.UserLoginRequest;
import com.reciprocityledger.backend.user.dto.request.UserRegisterRequest;
import com.reciprocityledger.backend.user.dto.response.UserLoginResponse;
import com.reciprocityledger.backend.user.entity.User;
import com.reciprocityledger.backend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final IdGenerator idGenerator;
    private final JwtService jwtService;

    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse register(UserRegisterRequest request) {
        final String email = normalizeEmail(request.getEmail());
        final User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTS, "该邮箱已注册");
        }

        final String encodedPassword = PasswordUtil.encode(request.getPassword());

        final User user = new User();
        user.setId(idGenerator.nextId());
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setNickname(normalizeNullableLength(request.getNickname(), 32));
        user.setAvatarUrl(null);
        user.setStatus("NORMAL");
        user.setLastLoginTime(LocalDateTime.now());

        userMapper.insert(user);

        final String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new UserLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                token
        );
    }

    public UserLoginResponse login(UserLoginRequest request) {
        final String email = normalizeEmail(request.getEmail());
        final User user = userMapper.selectByEmail(email);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        if (!"NORMAL".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被停用");
        }

        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "密码错误");
        }

        userMapper.updateLastLoginTime(user.getId());

        final String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new UserLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                token
        );
    }

    private String normalizeEmail(String email) {
        final String normalized = StrUtil.trim(email);
        if (StrUtil.isBlank(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "email不能为空");
        }
        return normalized.toLowerCase();
    }

    private String normalizeNullableLength(String value, int maxLength) {
        final String normalized = StrUtil.trim(value);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "字段长度超出限制");
        }
        return normalized;
    }
}
