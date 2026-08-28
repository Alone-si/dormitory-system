package org.example.bwxw.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Entity
@Table(name = "notices")
@Data
public class Notice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title; // 通知标题
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 通知内容
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeType type; // 通知类型
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticePriority priority; // 优先级
    
    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher; // 发布人（管理员）
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NoticeTarget target; // 目标对象（全体/男生/女生）
    
    @Column(nullable = false)
    private Boolean pinned = false; // 是否置顶
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 发布时间
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum NoticeType {
        ANNOUNCEMENT, // 公告
        MAINTENANCE,  // 维护通知
        EVENT,        // 活动通知
        REGULATION,   // 规章制度
        EMERGENCY     // 紧急通知
    }
    
    public enum NoticePriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
    
    public enum NoticeTarget {
        ALL,    // 全体
        MALE,   // 男生
        FEMALE  // 女生
    }
}
