package org.example.bwxw.repository;

import org.example.bwxw.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 根据配置类型查找配置
     */
    List<SystemConfig> findByType(SystemConfig.ConfigType type);
    
    /**
     * 查找所有启用的配置
     */
    List<SystemConfig> findByEnabledTrue();
    
    /**
     * 根据配置键和启用状态查找
     */
    Optional<SystemConfig> findByConfigKeyAndEnabledTrue(String configKey);
}
