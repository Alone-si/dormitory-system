package org.example.bwxw.dto;

import lombok.Data;
import org.example.bwxw.entity.User;

/**
 * 管理员创建/更新请求DTO
 */
@Data
public class AdminRequest {
    private String name;        // 姓名
    private String username;    // 用户名（登录账号）
    private String phone;       // 电话号码
    private User.AdminType adminType; // 管理员类型
}
