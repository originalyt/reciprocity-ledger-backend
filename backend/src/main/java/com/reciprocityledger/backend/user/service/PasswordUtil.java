package com.reciprocityledger.backend.user.service;


import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordUtil {

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
