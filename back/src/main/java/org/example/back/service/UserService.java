package org.example.back.service;

import org.example.back.entity.User;

import java.util.Map;

public interface UserService {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(User user);
    
    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);
    
    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 更新用户信息
     */
    boolean updateUserInfo(User user);
    
    /**
     * 退出登录
     */
    void logout(Long userId);
    
    /**
     * 忘记密码 - 验证手机号
     */
    User verifyPhone(String phone);
    
    /**
     * 发送验证码
     */
    String sendVerifyCode(String phone);
    
    /**
     * 验证验证码
     */
    boolean verifyCode(String phone, String code);
    
    /**
     * 发送注册验证码（用于注册时验证手机号）
     */
    String sendRegisterCode(String phone);
    
    /**
     * 重置密码
     */
    boolean resetPassword(String phone, String password);
}
