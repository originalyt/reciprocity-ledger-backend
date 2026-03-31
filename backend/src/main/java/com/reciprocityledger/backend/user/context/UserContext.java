package com.reciprocityledger.backend.user.context;

/**
 * 用户上下文，用于存储当前登录用户信息。
 * 使用 ThreadLocal 保证线程安全。
 */
public class UserContext {

    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

    private static final ThreadLocal<String> emailHolder = new ThreadLocal<>();

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static void setEmail(String email) {
        emailHolder.set(email);
    }

    public static String getEmail() {
        return emailHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        emailHolder.remove();
    }
}
