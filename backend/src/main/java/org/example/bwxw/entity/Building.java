package org.example.bwxw.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 宿舍楼实体类
 */
@Entity
@Table(name = "buildings")
@Data
public class Building {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name; // 楼栋名称，如 "1号楼"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender type; // MALE 或 FEMALE（男女分楼）
    
    @Column(nullable = false)
    private Integer floors; // 楼层数
    
    @Column(nullable = false)
    private Integer roomsPerFloor; // 每层房间数
    
    @Column(nullable = false)
    private Integer capacity; // 每间房间容量
    
    @Column(length = 200)
    private String address;
    
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Room> rooms;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum Gender {
        MALE,
        FEMALE
    }
}
