package org.example.bwxw.service;

import org.example.bwxw.entity.SystemConfig;
import org.example.bwxw.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    /**
     * 获取配置值
     */
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKeyAndEnabledTrue(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }
    
    /**
     * 获取配置值，如果不存在返回默认值
     */
    public String getConfigValue(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 设置配置值
     */
    public SystemConfig setConfig(String key, String value, String description, SystemConfig.ConfigType type) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(key);
        
        SystemConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfigValue(value);
            config.setDescription(description);
            config.setType(type);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            config.setType(type);
            config.setEnabled(true);
        }
        
        return systemConfigRepository.save(config);
    }
    
    /**
     * 获取所有配置
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }
    
    /**
     * 获取所有启用的配置
     */
    public List<SystemConfig> getEnabledConfigs() {
        return systemConfigRepository.findByEnabledTrue();
    }
    
    /**
     * 启用/禁用配置
     */
    public void toggleConfig(String key, boolean enabled) {
        systemConfigRepository.findByConfigKey(key).ifPresent(config -> {
            config.setEnabled(enabled);
            systemConfigRepository.save(config);
        });
    }
}
