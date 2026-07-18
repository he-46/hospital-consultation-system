package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.JwtUtil;
import org.example.back.common.PasswordUtil;
import org.example.back.common.RedisUtil;
import org.example.back.config.AliyunSmsConfig;
import org.example.back.entity.User;
import org.example.back.mapper.UserMapper;
import org.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AliyunSmsConfig aliyunSmsConfig;

    private static final String REDIS_TOKEN_PREFIX = "user:token:";

    @Override
    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).or().eq(User::getPhone, username);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 使用MD5+盐码验证密码
        if (!PasswordUtil.verifyStoredPassword(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 存储到Redis
        redisUtil.set(REDIS_TOKEN_PREFIX + user.getId(), token, 24, TimeUnit.HOURS);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        
        return result;
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, user.getPhone());
            if (this.count(wrapper) > 0) {
                throw new RuntimeException("手机号已被注册");
            }
        }
        
        // 使用MD5+盐码加密密码
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encryptPassword(user.getPassword(), salt);
        // 存储格式：加密密码:盐码
        user.setPassword(encryptedPassword + ":" + salt);
        
        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        // 只有没有上传头像时才设置默认头像
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("/img/default-avatar.png");
        }
        
        return this.save(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 使用MD5+盐码验证原密码
        if (!PasswordUtil.verifyStoredPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 生成新的盐码并加密新密码
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encryptPassword(newPassword, salt);
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getPassword, encryptedPassword + ":" + salt)
                .set(User::getUpdateTime, LocalDateTime.now());
        
        return this.update(wrapper);
    }

    @Override
    public boolean updateUserInfo(User user) {
        User existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(null); // 不允许修改密码
        user.setUsername(null); // 不允许修改用户名
        user.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(user);
    }

    @Override
    public void logout(Long userId) {
        redisUtil.delete(REDIS_TOKEN_PREFIX + userId);
    }

    @Override
    public User verifyPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        
        return user;
    }
    
    private static final String REDIS_CODE_PREFIX = "verify:code:";

    @Override
    public String sendVerifyCode(String phone) {
        // 60秒冷却
        if (redisUtil.get("sms:cooldown:" + phone) != null) {
            throw new RuntimeException("发送太频繁，请60秒后再试");
        }
        
        // 先验证手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        
        // 生成6位随机验证码
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        // 存储到Redis，5分钟有效
        redisUtil.set(REDIS_CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        
        // 发送短信验证码（阿里云）
        try {
            aliyunSmsConfig.getAliyunSmsUtil().sendVerifyCode(phone, aliyunSmsConfig.getSignName(), aliyunSmsConfig.getTemplateCode(), code);
        } catch (Exception e) {
            throw new RuntimeException("验证码发送失败: " + e.getMessage());
        }
        
        // 60秒冷却
        redisUtil.set("sms:cooldown:" + phone, "1", 60, TimeUnit.SECONDS);
        
        return code;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String storedCode = (String) redisUtil.get(REDIS_CODE_PREFIX + phone);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        // 验证成功后删除验证码
        redisUtil.delete(REDIS_CODE_PREFIX + phone);
        return true;
    }
    
    private static final String REDIS_REG_CODE_PREFIX = "register:code:";

    @Override
    public String sendRegisterCode(String phone) {
        // 60秒冷却
        if (redisUtil.get("sms:cooldown:" + phone) != null) {
            throw new RuntimeException("发送太频繁，请60秒后再试");
        }
        
        // 检查手机号是否已被注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        if (user != null) {
            throw new RuntimeException("该手机号已被注册");
        }
        
        // 生成6位随机验证码
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        // 存储到Redis，5分钟有效
        redisUtil.set(REDIS_REG_CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
        
        // 发送短信验证码（阿里云）
        try {
            aliyunSmsConfig.getAliyunSmsUtil().sendVerifyCode(phone, aliyunSmsConfig.getSignName(), aliyunSmsConfig.getTemplateCode(), code);
        } catch (Exception e) {
            throw new RuntimeException("验证码发送失败: " + e.getMessage());
        }
        
        // 60秒冷却
        redisUtil.set("sms:cooldown:" + phone, "1", 60, TimeUnit.SECONDS);
        
        return code;
    }

    @Override
    public boolean resetPassword(String phone, String password) {
        // 使用MD5+盐码加密密码
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encryptPassword(password, salt);
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getPhone, phone)
                .set(User::getPassword, encryptedPassword + ":" + salt)
                .set(User::getUpdateTime, LocalDateTime.now());
        
        return this.update(wrapper);
    }
}
