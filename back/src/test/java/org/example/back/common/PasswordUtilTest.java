package org.example.back.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PasswordUtil 单元测试
 * 覆盖：MD5加密、盐码生成、密码存储、双重验证
 */
class PasswordUtilTest {

    @Test
    void testGenerateSalt_returnsNonEmptyString() {
        String salt = PasswordUtil.generateSalt();
        Assertions.assertNotNull(salt);
        Assertions.assertFalse(salt.isEmpty());
    }

    @Test
    void testGenerateSalt_producesUniqueValues() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        Assertions.assertNotEquals(salt1, salt2, "每次生成的盐码应不同");
    }

    @Test
    void testEncryptPassword_sameInputSameSalt_sameOutput() {
        String encrypted1 = PasswordUtil.encryptPassword("123456", "testSalt");
        String encrypted2 = PasswordUtil.encryptPassword("123456", "testSalt");
        Assertions.assertEquals(encrypted1, encrypted2);
    }

    @Test
    void testEncryptPassword_differentSalt_differentOutput() {
        String encrypted1 = PasswordUtil.encryptPassword("123456", "salt1");
        String encrypted2 = PasswordUtil.encryptPassword("123456", "salt2");
        Assertions.assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testEncryptPassword_differentPassword_differentOutput() {
        String encrypted1 = PasswordUtil.encryptPassword("123456", "salt");
        String encrypted2 = PasswordUtil.encryptPassword("654321", "salt");
        Assertions.assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testMd5_returns32CharHex() {
        String hash = PasswordUtil.md5("hello");
        Assertions.assertEquals(32, hash.length());
        Assertions.assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    void testMd5_deterministic() {
        Assertions.assertEquals(PasswordUtil.md5("test"), PasswordUtil.md5("test"));
    }

    @Test
    void testEncryptAndStore_containsColon() {
        String stored = PasswordUtil.encryptAndStore("mypassword");
        Assertions.assertTrue(stored.contains(":"), "存储格式应含冒号");
        String[] parts = stored.split(":");
        Assertions.assertEquals(2, parts.length);
    }

    @Test
    void testVerifyStoredPassword_withNull_returnsFalse() {
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("123456", null));
    }

    @Test
    void testVerifyStoredPassword_plainText_match() {
        // 模拟旧数据：明文存储
        Assertions.assertTrue(PasswordUtil.verifyStoredPassword("123456", "123456"));
    }

    @Test
    void testVerifyStoredPassword_plainText_mismatch() {
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("wrong", "123456"));
    }

    @Test
    void testVerifyStoredPassword_md5WithSalt_match() {
        // 新数据：MD5+salt 格式
        String stored = PasswordUtil.encryptAndStore("mypassword");
        Assertions.assertTrue(PasswordUtil.verifyStoredPassword("mypassword", stored));
    }

    @Test
    void testVerifyStoredPassword_md5WithSalt_mismatch() {
        String stored = PasswordUtil.encryptAndStore("mypassword");
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("wrongpassword", stored));
    }

    @Test
    void testVerifyStoredPassword_newUserFlow() {
        // 模拟注册→登录全流程
        String rawPassword = "userPass@123";
        String stored = PasswordUtil.encryptAndStore(rawPassword);

        // 正确密码
        Assertions.assertTrue(PasswordUtil.verifyStoredPassword(rawPassword, stored));

        // 错误密码
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("wrong", stored));

        // 空密码
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("", stored));
    }

    @Test
    void testVerifyStoredPassword_oldUserCompatibility() {
        // 模拟旧数据库账号（明文 123456）
        Assertions.assertTrue(PasswordUtil.verifyStoredPassword("123456", "123456"));
        Assertions.assertFalse(PasswordUtil.verifyStoredPassword("wrong", "123456"));
    }

    @Test
    void testMd5_emptyString() {
        String hash = PasswordUtil.md5("");
        Assertions.assertEquals(32, hash.length());
        Assertions.assertEquals("d41d8cd98f00b204e9800998ecf8427e", hash);
    }

    @ParameterizedTest
    @CsvSource({
        "password123, 123456, false",
        "samepass, samepass, true",
        "Hello, Hello, true",
        "admin, admin123, false"
    })
    void parameterizedTest_plainText(String input, String stored, boolean expected) {
        Assertions.assertEquals(expected, PasswordUtil.verifyStoredPassword(input, stored));
    }
}
