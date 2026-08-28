package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.dto.LoginRequest;
import org.example.bwxw.dto.LoginResponse;
import org.example.bwxw.filter.TokenAuthenticationFilter;
import org.example.bwxw.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            TokenAuthenticationFilter.removeToken(authHeader.substring(7));
        }
        return ApiResponse.success("退出登录成功", null);
    }
}
