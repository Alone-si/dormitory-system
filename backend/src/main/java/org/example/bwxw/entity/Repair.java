package org.example.bwxw.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报修申请实体类
 */
@Entity
@Table(name = "repairs")
@Data
public class Repair {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = true)
    private User student; // 报修学生（删除学生后为空）
    
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // 报修房间
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepairType type; // 报修类型
    
    @Column(nullable = false, length = 500)
    private String description; // 问题描述
    
    @Column(length = 200)
    private String location; // 具体位置
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepairStatus status; // 状态
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UrgencyLevel urgency; // 紧急程度
    
    @Column(length = 500)
    private String adminReply; // 管理员回复
    
    @ManyToOne
    @JoinColumn(name = "handler_id")
    private User handler; // 处理人（管理员）
    
    @Column(name = "handled_at")
    private LocalDateTime handledAt; // 处理时间
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RepairStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum RepairType {
        ELECTRICAL,  // 电路问题
        PLUMBING,    // 水管问题
        FURNITURE,   // 家具损坏
        DOOR_WINDOW, // 门窗问题
        NETWORK,     // 网络问题
        OTHER        // 其他
    }
    
    public enum RepairStatus {
        PENDING,     // 待处理
        IN_PROGRESS, // 处理中
        COMPLETED,   // 已完成
        REJECTED     // 已拒绝
    }
    
    public enum UrgencyLevel {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
