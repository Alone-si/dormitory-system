package org.example.bwxw.controller;

import org.example.bwxw.dto.ApiResponse;
import org.example.bwxw.entity.SystemConfig;
import org.example.bwxw.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取所有配置
     */
    @GetMapping
    public ApiResponse<List<SystemConfig>> getAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取指定配置值
     */
    @GetMapping("/{key}")
    public ApiResponse<String> getConfigValue(@PathVariable String key) {
        try {
            String value = systemConfigService.getConfigValue(key);
            if (value != null) {
                return ApiResponse.success(value);
            } else {
                return ApiResponse.error("配置不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 设置配置
     */
    @PostMapping
    public ApiResponse<SystemConfig> setConfig(@RequestBody Map<String, Object> params) {
        try {
            String key = (String) params.get("key");
            String value = (String) params.get("value");
            String description = (String) params.get("description");
            String typeStr = (String) params.get("type");
            
            SystemConfig.ConfigType type = SystemConfig.ConfigType.valueOf(typeStr.toUpperCase());
            
            SystemConfig config = systemConfigService.setConfig(key, value, description, type);
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 启用/禁用配置
     */
    @PutMapping("/{key}/toggle")
    public ApiResponse<String> toggleConfig(@PathVariable String key, @RequestBody Map<String, Boolean> params) {
        try {
            Boolean enabled = params.get("enabled");
            systemConfigService.toggleConfig(key, enabled);
            return ApiResponse.success("配置状态已更新");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
