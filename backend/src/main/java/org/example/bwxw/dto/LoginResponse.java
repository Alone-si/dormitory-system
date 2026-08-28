package org.example.bwxw.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.example.bwxw.entity.User;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User.UserRole role;
    private Long userId;
    private String username;
    private String name;
    private User user; // 完整的用户对象，包含room等关联数据
}
