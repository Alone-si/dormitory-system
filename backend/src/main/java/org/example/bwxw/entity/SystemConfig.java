package org.example.bwxw.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体类 - 存储系统级别的配置信息
 */
@Data
@Entity
@Table(name = "system_configs")
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 配置键名（唯一）
     */
    @Column(unique = true, nullable = false, length = 100)
    private String configKey;
    
    /**
     * 配置值
     */
    @Column(columnDefinition = "TEXT")
    private String configValue;
    
    /**
     * 配置描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 配置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigType type;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    public enum ConfigType {
        STRING,     // 字符串
        NUMBER,     // 数字
        BOOLEAN,    // 布尔值
        JSON,       // JSON对象
        ARRAY       // 数组
    }
}
