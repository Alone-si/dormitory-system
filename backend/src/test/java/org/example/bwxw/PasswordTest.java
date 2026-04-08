package org.example.bwxw;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 生成 123456 的 BCrypt 哈希
        String password = "123456";
        String hash = passwordEncoder.encode(password);
        
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt 哈希: " + hash);
        
        // 验证现有的哈希
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbmHNsZLQAqU2jRTC";
        boolean matches = passwordEncoder.matches(password, existingHash);
        
        System.out.println("\n验证现有哈希:");
        System.out.println("现有哈希: " + existingHash);
        System.out.println("是否匹配 123456: " + matches);
        
        // 生成新的哈希用于 SQL
        System.out.println("\n新生成的哈希（可用于 SQL）:");
        for (int i = 0; i < 3; i++) {
            System.out.println(passwordEncoder.encode(password));
        }
    }
}
