package org.example.back.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类 - MD5 + 盐码
 */
public class PasswordUtil {

    /**
     * 生成随机盐码
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * MD5加密（带盐码）
     * 加密方式：MD5(密码 + 盐码)
     */
    public static String encryptPassword(String password, String salt) {
        try {
            String combined = password + salt;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * 验证密码是否正确
     */
    public static boolean verifyPassword(String inputPassword, String storedPassword, String salt) {
        String encrypted = encryptPassword(inputPassword, salt);
        return encrypted.equals(storedPassword);
    }

    /**
     * 加密密码并返回（密码+盐码的格式存储）
     * 返回格式：MD5(密码+盐码):盐码
     */
    public static String encryptAndStore(String password) {
        String salt = generateSalt();
        String encrypted = encryptPassword(password, salt);
        return encrypted + ":" + salt;
    }

    /**
     * 验证存储的密码
     * @param inputPassword 用户输入的密码
     * @param storedPassword 存储的密码（格式：MD5(密码+盐码):盐码）
     * @return 是否匹配
     */
    public static boolean verifyStoredPassword(String inputPassword, String storedPassword) {
        if (storedPassword == null || !storedPassword.contains(":")) {
            // 兼容旧密码（直接MD5存储的密码）
            return md5(inputPassword).equals(storedPassword);
        }
        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        String encrypted = parts[0];
        String salt = parts[1];
        return encryptPassword(inputPassword, salt).equals(encrypted);
    }

    /**
     * 简单的MD5加密（无盐码）
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
