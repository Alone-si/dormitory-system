package org.example.bwxw.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserRepository userRepository;
    
    // 简单的token存储（生产环境应使用Redis等）
    private static final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    
    // 存储token和用户的映射
    public static void storeToken(String token, String userIdentifier) {
        tokenStore.put(token, userIdentifier);
    }
    
    // 移除token
    public static void removeToken(String token) {
        tokenStore.remove(token);
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        System.out.println("DEBUG: TokenFilter - Authorization header: " + authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("DEBUG: TokenFilter - Extracted token: " + token);
            
            // 从token存储中获取用户标识
            String userIdentifier = tokenStore.get(token);
            System.out.println("DEBUG: TokenFilter - User identifier from token: " + userIdentifier);
            
            if (userIdentifier != null) {
                try {
                    // 根据用户标识查找用户
                    User user = userRepository.findByStudentId(userIdentifier)
                            .or(() -> userRepository.findByUsername(userIdentifier))
                            .orElse(null);
                    
                    if (user != null) {
                        System.out.println("DEBUG: TokenFilter - Found user: " + user.getName() + ", studentId: " + user.getStudentId());
                        
                        // 创建认证对象
                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                        );
                        
                        // 使用学号作为principal（如果存在），否则使用用户名
                        String principal = user.getStudentId() != null && !user.getStudentId().isEmpty() 
                                ? user.getStudentId() 
                                : user.getUsername();
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("DEBUG: TokenFilter - Authentication set for user: " + principal);
                    } else {
                        System.out.println("DEBUG: TokenFilter - User not found for identifier: " + userIdentifier);
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: TokenFilter - Error finding user: " + e.getMessage());
                }
            } else {
                System.out.println("DEBUG: TokenFilter - Token not found in store");
            }
        } else {
            System.out.println("DEBUG: TokenFilter - No valid Authorization header");
        }
        
        filterChain.doFilter(request, response);
    }
}
