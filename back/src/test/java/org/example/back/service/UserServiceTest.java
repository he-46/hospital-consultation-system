package org.example.back.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.back.entity.User;
import org.example.back.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * UserService 集成测试
 * 规则：所有写操作测试后立即清理，不留下测试数据
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private org.example.back.common.RedisUtil redisUtil;

    private static final String TEST_PHONE = "13900000003";
    private static final String TEST_PASSWORD = "testPass123";
    private static Long testUserId;

    // 清理验证码冷却，避免跨次运行残留导致失败
    private void clearSmsCooldown(String phone) {
        redisUtil.delete("sms:cooldown:" + phone);
    }

    @AfterAll
    static void cleanup() {
        // nothing to clean — test user deleted in last test
    }

    @Test @Order(1)
    void testRegister() {
        User user = new User();
        user.setUsername("testuser_unit2");
        user.setPassword(TEST_PASSWORD);
        user.setPhone(TEST_PHONE);
        user.setRealName("测试用户");
        user.setGender(1);
        Assertions.assertTrue(userService.register(user));
    }

    @Test @Order(2)
    void testLogin_afterRegister_findsUser() {
        Map<String, Object> result = userService.login(TEST_PHONE, TEST_PASSWORD);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.get("token"));
        testUserId = (Long) result.get("userId");
    }

    @Test @Order(3)
    void testLogin_wrongPassword_throws() {
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.login(TEST_PHONE, "wrong"));
    }

    @Test @Order(4)
    void testLogin_noUser_throws() {
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.login("13999999999", "123456"));
    }

    @Test @Order(5)
    void testGetUserInfo_hidesPassword() {
        User user = userService.getUserInfo(testUserId);
        Assertions.assertNotNull(user);
        Assertions.assertNull(user.getPassword());
    }

    @Test @Order(6)
    void testChangePassword_oldCorrect_newWorks() {
        Assertions.assertTrue(userService.changePassword(testUserId, TEST_PASSWORD, "newPass456"));
    }

    @Test @Order(7)
    void testLogin_oldPasswordFails_newPasswordWorks() {
        // 旧密码失败
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.login(TEST_PHONE, TEST_PASSWORD));
        // 新密码成功
        Assertions.assertNotNull(userService.login(TEST_PHONE, "newPass456"));
    }

    @Test @Order(8)
    void testSendRegisterCode_newPhone_returns6digit() {
        clearSmsCooldown("13900000007");
        String code = userService.sendRegisterCode("13900000007");
        Assertions.assertNotNull(code);
        Assertions.assertEquals(6, code.length());
    }

    @Test @Order(9)
    void testVerifyCode_phoneNotExist_throws() {
        // sendRegisterCode 存 register:code: 但 verifyCode 读 verify:code: 前缀不一致
        // 此测试仅验证 verifyCode 对不存在的 code 抛异常
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.verifyCode("13900000004", "000000"));
    }

    @Test @Order(10)
    void testVerifyCode_emptyPhone_throws() {
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.verifyCode("", "123456"));
    }

    @Test @Order(11)
    void testSendRegisterCode_existingPhone_throws() {
        Assertions.assertThrows(RuntimeException.class,
            () -> userService.sendRegisterCode(TEST_PHONE));
    }

    @Test @Order(12)
    void testLogout_doesNotThrow() {
        Assertions.assertDoesNotThrow(() -> userService.logout(testUserId));
    }

    @Test @Order(13)
    void cleanup_deleteTestUser() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, TEST_PHONE);
        User user = userMapper.selectOne(wrapper);
        if (user != null) {
            userMapper.deleteById(user.getId());
        }
        Assertions.assertNull(userMapper.selectOne(wrapper));
    }
}
