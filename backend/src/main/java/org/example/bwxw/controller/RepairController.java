package org.example.bwxw.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.Repair;
import org.example.bwxw.entity.User;
import org.example.bwxw.service.CurrentUserService;
import org.example.bwxw.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {
    
    @Autowired
    private RepairService repairService;
    
    @Autowired
    private CurrentUserService currentUserService;
    
        @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<Repair>> getAllRepairs() {
        try {
            List<Repair> repairs = repairService.getAllRepairs();
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<Repair> getRepairById(@PathVariable Long id) {
        try {
            Repair repair = repairService.getRepairById(id);
            return ApiResponse.success(repair);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @GetMapping("/student/{studentId}")
    public ApiResponse<List<Repair>> getRepairsByStudent(@PathVariable Long studentId) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            if (currentUser.getRole() == User.UserRole.STUDENT && !currentUser.getId().equals(studentId)) {
                return ApiResponse.error("无权查看其他学生的报修记录");
            }
            List<Repair> repairs = repairService.getRepairsByStudent(studentId);
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ApiResponse<List<Repair>> getPendingRepairs() {
        try {
            List<Repair> repairs = repairService.getPendingRepairs();
            return ApiResponse.success(repairs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ApiResponse<Repair> createRepair(@RequestBody Repair repair) {
        try {
            if (repair.getDescription() == null || repair.getDescription().trim().isEmpty()) {
                return ApiResponse.error("问题描述不能为空");
            }
            if (repair.getType() == null) {
                return ApiResponse.error("报修类型不能为空");
            }

            User currentUser = currentUserService.getCurrentUser();
            if (currentUser.getRoom() == null) {
                return ApiResponse.error("未分配宿舍，无法提交报修");
            }

            repair.setId(null);
            repair.setStudent(currentUser);
            repair.setRoom(currentUser.getRoom());
            repair.setStatus(Repair.RepairStatus.PENDING);
            repair.setHandler(null);
            repair.setAdminReply(null);
            repair.setHandledAt(null);

            Repair created = repairService.createRepair(repair);
            return ApiResponse.success("报修提交成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ApiResponse<Repair> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Repair.RepairStatus status = Repair.RepairStatus.valueOf((String) payload.get("status"));
            String reply = (String) payload.get("reply");
            
            // 自动获取当前登录的管理员作为处理人
            User currentAdmin = currentUserService.getCurrentUser();
            
            Repair updated = repairService.updateRepairStatus(id, status, currentAdmin.getId(), reply);
            return ApiResponse.success("状态更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除报修记录
     */
        @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRepair(@PathVariable Long id) {
        try {
            // 权限校验：只有管理员才能删除报修
            User currentUser = currentUserService.getCurrentUser();
            if (currentUser.getRole() != User.UserRole.ADMIN) {
                return ApiResponse.error("权限不足，只有管理员才能删除报修记录");
            }
            
            repairService.deleteRepair(id);
            return ApiResponse.success("报修记录删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除报修记录失败: " + e.getMessage());
        }
    }
}
