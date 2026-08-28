package org.example.bwxw.dto;

import lombok.Data;
import org.example.bwxw.entity.User;
import java.time.LocalDateTime;

/**
 * 管理员响应DTO
 */
@Data
public class AdminResponse {
    private Long id;
    private String name;        // 姓名
    private String username;    // 用户名
    private String phone;       // 电话号码
    private String status;      // 状态
    private User.AdminType adminType; // 管理员类型
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
