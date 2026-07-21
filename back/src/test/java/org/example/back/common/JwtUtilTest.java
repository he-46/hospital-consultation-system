package org.example.back.common;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * JwtUtil 单元测试
 */
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    private void initUtil(String secret, long expirationMs) throws Exception {
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, secret);
        Field expField = JwtUtil.class.getDeclaredField("expiration");
        expField.setAccessible(true);
        expField.set(jwtUtil, expirationMs);
    }

    @Test void testGenerateToken() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 3600000L);
        String token = jwtUtil.generateToken(1L, "test");
        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.split("\\.").length == 3);
    }

    @Test void testParseToken_userId() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 3600000L);
        String token = jwtUtil.generateToken(42L, "user42");
        Assertions.assertEquals(42L, jwtUtil.getUserIdFromToken(token));
    }

    @Test void testParseToken_username() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 3600000L);
        String token = jwtUtil.generateToken(1L, "zhangsan");
        Assertions.assertEquals("zhangsan", jwtUtil.getUsernameFromToken(token));
    }

    @Test void testDifferentUsersDiffTokens() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 3600000L);
        Assertions.assertNotEquals(
            jwtUtil.generateToken(1L, "a"),
            jwtUtil.generateToken(2L, "b"));
    }

    @Test void testIsTokenExpired_notExpired() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 86400000L);
        String token = jwtUtil.generateToken(1L, "test");
        Assertions.assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test void testParseToken_valid() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 86400000L);
        Claims claims = jwtUtil.parseToken(jwtUtil.generateToken(1L, "test"));
        Assertions.assertNotNull(claims);
    }

    @Test void testLargeUserId() throws Exception {
        initUtil("test-secret-key-needs-32-bytes-minimum", 3600000L);
        String token = jwtUtil.generateToken(999999999L, "test");
        Assertions.assertEquals(999999999L, jwtUtil.getUserIdFromToken(token));
    }
}
