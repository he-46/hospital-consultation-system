package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.entity.User;
import org.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || username.isEmpty()) {
            return Result.error("请输入用户名");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("请输入密码");
        }
        
        try {
            Map<String, Object> result = userService.login(username, password);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Result.error("请输入用户名");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("请输入密码");
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            return Result.error("请输入手机号");
        }
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            return Result.error("请输入真实姓名");
        }
        if (user.getGender() == null) {
            return Result.error("请选择性别");
        }
        if (user.getBirthday() == null) {
            return Result.error("请选择生日");
        }
        
        try {
            boolean result = userService.register(user);
            if (result) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("请输入新密码");
        }
        
        try {
            boolean result = userService.changePassword(userId, oldPassword, newPassword);
            if (result) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result<?> updateUserInfo(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        
        try {
            boolean result = userService.updateUserInfo(user);
            if (result) {
                return Result.success("信息更新成功");
            } else {
                return Result.error("信息更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            userService.logout(userId);
        }
        return Result.success("退出成功");
    }

    /**
     * 忘记密码 - 验证手机号
     */
    @PostMapping("/verifyPhone")
    public Result<?> verifyPhone(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        
        if (phone == null || phone.isEmpty()) {
            return Result.error("请输入手机号");
        }
        
        try {
            User user = userService.verifyPhone(phone);
            // 返回用户名用于提示（不返回完整信息）
            Map<String, String> result = new java.util.HashMap<>();
            result.put("username", user.getUsername());
            return Result.success("验证成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 忘记密码 - 重置密码
     */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        
        if (phone == null || phone.isEmpty()) {
            return Result.error("请输入手机号");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("请输入新密码");
        }
        
        try {
            boolean result = userService.resetPassword(phone, password);
            if (result) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
