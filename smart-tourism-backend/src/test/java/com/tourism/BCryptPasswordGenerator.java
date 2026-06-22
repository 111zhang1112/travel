package com.tourism;

import cn.dev33.satoken.secure.BCrypt;

/**
 * BCrypt密码生成器
 * 用于生成商家测试账号的密码哈希
 */
public class BCryptPasswordGenerator {
    
    public static void main(String[] args) {
        String password = "123456";
        
        // 使用Sa-Token的BCrypt生成密码哈希
        String hashedPassword = BCrypt.hashpw(password);
        
        System.out.println("=== BCrypt密码生成器 ===");
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt哈希: " + hashedPassword);
        System.out.println();
        
        // 验证生成的哈希是否正确
        boolean isMatch = BCrypt.checkpw(password, hashedPassword);
        System.out.println("验证结果: " + (isMatch ? "✓ 匹配成功" : "✗ 匹配失败"));
        System.out.println();
        
        // 生成SQL更新语句
        System.out.println("=== SQL更新语句 ===");
        System.out.println("UPDATE merchant_users SET password = '" + hashedPassword + "' WHERE username = 'scenic_test';");
        System.out.println("UPDATE merchant_users SET password = '" + hashedPassword + "' WHERE username = 'hotel_test';");
    }
}
