package com.reciprocityledger.backend.user.service;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:reciprocity-ledger-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private JWTSigner getSigner() {
        return JWTSignerUtil.hs512(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("email", email)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiresAt(expiryDate)
                .setSigner(getSigner())
                .sign();
    }

    public JWT parseToken(String token) {
        return JWT.of(token);
    }

    public String getUserIdFromToken(String token) {
        JWT jwt = parseToken(token);
        return (String) jwt.getPayload("userId");
    }

    public String getEmailFromToken(String token) {
        JWT jwt = parseToken(token);
        return (String) jwt.getPayload("email");
    }

    public boolean validateToken(String token) {
        try {
            JWT jwt = JWT.of(token);
            if (!jwt.setSigner(getSigner()).verify()) {
                return false;
            }
            JWTValidator validator = JWTValidator.of(jwt);
            validator.validateDate(new Date());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
