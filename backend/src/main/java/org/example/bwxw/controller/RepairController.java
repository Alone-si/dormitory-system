package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.Repair;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.example.bwxw.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = "*")
public class RepairController {
    
    @Autowired
    private RepairService repairService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ApiResponse<List<Repair>> getAllRepairs() {
        try {
            List<Repair> repairs = repairService.getAllRepairs();
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Repair> getRepairById(@PathVariable Long id) {
        try {
            Repair repair = repairService.getRepairById(id);
            return ApiResponse.success(repair);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/student/{studentId}")
    public ApiResponse<List<Repair>> getRepairsByStudent(@PathVariable Long studentId) {
        try {
            List<Repair> repairs = repairService.getRepairsByStudent(studentId);
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public ApiResponse<List<Repair>> getPendingRepairs() {
        try {
            List<Repair> repairs = repairService.getPendingRepairs();
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Repair> createRepair(@RequestBody Repair repair) {
        try {
            Repair created = repairService.createRepair(repair);
            return ApiResponse.success("报修提交成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public ApiResponse<Repair> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Repair.RepairStatus status = Repair.RepairStatus.valueOf((String) payload.get("status"));
            String reply = (String) payload.get("reply");
            
            // 自动获取当前登录的管理员作为处理人
            User currentAdmin = getCurrentAuthenticatedUser();
            
            Repair updated = repairService.updateRepairStatus(id, status, currentAdmin.getId(), reply);
            return ApiResponse.success("状态更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除报修记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRepair(@PathVariable Long id) {
        try {
            // 权限校验：只有管理员才能删除报修
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser.getRole() != User.UserRole.ADMIN) {
                return ApiResponse.error("权限不足，只有管理员才能删除报修记录");
            }
            
            repairService.deleteRepair(id);
            return ApiResponse.success("报修记录删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除报修记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录的用户
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByStudentId(username)
            .or(() -> userRepository.findByUsername(username))
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
