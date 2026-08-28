package org.example.bwxw.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体类
 */
@Entity
@Table(name = "leaves")
@Data
public class Leave {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = true)
    private User student; // 请假学生（删除学生后为空）
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveType type; // 请假类型
    
    @Column(nullable = false)
    private LocalDate startDate; // 开始日期
    
    @Column(nullable = false)
    private LocalDate endDate; // 结束日期
    
    @Column(nullable = false)
    private Integer days; // 请假天数
    
    @Column(nullable = false, length = 500)
    private String reason; // 请假原因
    
    @Column(length = 100)
    private String emergencyContact; // 紧急联系人
    
    @Column(length = 20)
    private String emergencyPhone; // 紧急联系电话
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status; // 审批状态
    
    @Column(length = 500)
    private String adminComment; // 管理员审批意见
    
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver; // 审批人（管理员）
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 审批时间
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LeaveStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum LeaveType {
        SICK,        // 病假
        PERSONAL,    // 事假
        FAMILY,      // 家事假
        EMERGENCY,   // 紧急事假
        OTHER        // 其他
    }
    
    public enum LeaveStatus {
        PENDING,     // 待审批
        APPROVED,    // 已批准
        REJECTED     // 已拒绝
    }
}
