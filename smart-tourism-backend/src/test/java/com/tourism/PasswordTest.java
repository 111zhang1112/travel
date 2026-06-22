package com.tourism;

import cn.dev33.satoken.secure.BCrypt;

/**
 * 密码测试工具
 * 用于生成和验证BCrypt密码
 */
public class PasswordTest {
    
    public static void main(String[] args) {
        String plainPassword = "123456";
        
        // 生成新的BCrypt密码
        String newHash = BCrypt.hashpw(plainPassword);
        System.out.println("新生成的BCrypt密码: " + newHash);
        
        // 验证标准BCrypt密码（Laravel使用的）
        String standardHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        boolean isStandardValid = BCrypt.checkpw(plainPassword, standardHash);
        System.out.println("标准BCrypt密码验证: " + isStandardValid);
        
        // 验证数据库中的密码
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKfGPu6";
        boolean isDbValid = BCrypt.checkpw(plainPassword, dbHash);
        System.out.println("数据库BCrypt密码验证: " + isDbValid);
        
        // 测试新生成的密码
        boolean isNewValid = BCrypt.checkpw(plainPassword, newHash);
        System.out.println("新生成密码验证: " + isNewValid);
    }
}
