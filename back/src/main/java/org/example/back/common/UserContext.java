package org.example.back.common;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    // 存入当前登录用户ID
    public static void setUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    // 获取当前登录用户ID
    public static Long getUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    // 清除，防止内存泄漏
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }
}