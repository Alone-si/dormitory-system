package org.example.bwxw.service;

import org.example.bwxw.dto.LoginRequest;
import org.example.bwxw.dto.LoginResponse;
import org.example.bwxw.entity.User;
import org.example.bwxw.filter.TokenAuthenticationFilter;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public LoginResponse login(LoginRequest request) {
        // 优先用学号登录（学生），如果找不到再用用户名登录（管理员）
        User user = userRepository.findByStudentId(request.getUsername())
                .or(() -> userRepository.findByUsername(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        System.out.println("DEBUG: Login attempt - Username: " + request.getUsername());
        System.out.println("DEBUG: User found - Role: " + user.getRole() + ", AdminType: " + user.getAdminType());
        System.out.println("DEBUG: Password in DB (first 30 chars): " + user.getPassword().substring(0, Math.min(30, user.getPassword().length())));
        
        // 密码验证：超级管理员使用加密密码，其他用户使用明文密码
        boolean passwordMatches;
        if (user.getRole() == User.UserRole.ADMIN && user.getAdminType() == User.AdminType.SUPER_ADMIN) {
            // 超级管理员：使用BCrypt验证
            System.out.println("DEBUG: Using BCrypt validation for SUPER_ADMIN");
            passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("DEBUG: BCrypt match result: " + passwordMatches);
        } else {
            // 普通管理员和学生：使用明文验证
            System.out.println("DEBUG: Using plaintext validation");
            passwordMatches = request.getPassword().equals(user.getPassword());
            System.out.println("DEBUG: Plaintext match result: " + passwordMatches);
        }
        
        if (!passwordMatches) {
            throw new RuntimeException("密码错误");
        }
        
        // 生成简单token（实际应使用JWT）
        String token = UUID.randomUUID().toString();
        
        // 存储token和用户的映射
        String userIdentifier = user.getStudentId() != null && !user.getStudentId().isEmpty() 
                ? user.getStudentId() 
                : user.getUsername();
        TokenAuthenticationFilter.storeToken(token, userIdentifier);
        
        System.out.println("DEBUG: AuthService - Generated token: " + token + " for user: " + userIdentifier);
        
        return new LoginResponse(
                token,
                user.getRole(),
                user.getId(),
                user.getUsername(),
                user.getName(),
                user  // 返回完整的用户对象
        );
    }
    
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }
}
