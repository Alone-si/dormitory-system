package org.example.bwxw.service;

import org.example.bwxw.entity.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Service;  // 已禁用

/**
 * 数据迁移服务 - 将硬编码数据迁移到数据库配置表
 * 已禁用：不自动注入数据，只创建表结构
 */
// @Service  // 已注释：禁用自动数据注入
@Order(2) // 在DataInitializer之后执行
public class DataMigrationService implements CommandLineRunner {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Override
    public void run(String... args) throws Exception {
        // migrateHardcodedData();  // 已注释：不自动注入系统配置数据
    }
    
    /**
     * 迁移硬编码数据到数据库
     * 注意：此方法已禁用，保留用于将来可能的数据迁移需求
     */
    @SuppressWarnings("unused")
    private void migrateHardcodedData() {
        // 1. 迁移床位数选项
        migrateCapacityOptions();
        
        // 2. 迁移状态筛选选项
        migrateStatusFilters();
        
        // 3. 迁移文件类型限制
        migrateFileTypeRestrictions();
        
        // 4. 迁移默认密码配置
        migrateDefaultPassword();
        
        // 5. 迁移学生姓名库
        migrateStudentNames();
        
        // 6. 迁移电话号码生成规则
        migratePhoneNumberRules();
    }
    
    /**
     * 迁移床位数选项
     */
    private void migrateCapacityOptions() {
        String[] capacityOptions = {"2", "4", "6"};
        String capacityJson = String.join(",", capacityOptions);
        
        systemConfigService.setConfig(
            "dormitory.capacity.options",
            capacityJson,
            "宿舍床位数选项",
            SystemConfig.ConfigType.ARRAY
        );
    }
    
    /**
     * 迁移状态筛选选项
     */
    private void migrateStatusFilters() {
        // 请假状态筛选
        systemConfigService.setConfig(
            "leave.status.filters",
            "[{\"label\":\"待审批\",\"value\":\"PENDING\"},{\"label\":\"已批准\",\"value\":\"APPROVED\"},{\"label\":\"已拒绝\",\"value\":\"REJECTED\"}]",
            "请假状态筛选选项",
            SystemConfig.ConfigType.JSON
        );
        
        // 报修状态筛选
        systemConfigService.setConfig(
            "repair.status.filters",
            "[{\"label\":\"全部\",\"value\":\"ALL\"},{\"label\":\"待处理\",\"value\":\"PENDING\"},{\"label\":\"处理中\",\"value\":\"IN_PROGRESS\"},{\"label\":\"已完成\",\"value\":\"COMPLETED\"},{\"label\":\"已拒绝\",\"value\":\"REJECTED\"}]",
            "报修状态筛选选项",
            SystemConfig.ConfigType.JSON
        );
    }
    
    /**
     * 迁移文件类型限制
     */
    private void migrateFileTypeRestrictions() {
        systemConfigService.setConfig(
            "upload.avatar.allowed.types",
            "image/jpeg,image/jpg,image/png,image/webp",
            "头像上传允许的文件类型",
            SystemConfig.ConfigType.ARRAY
        );
        
        systemConfigService.setConfig(
            "upload.avatar.max.size",
            "5242880",
            "头像上传最大文件大小（字节）",
            SystemConfig.ConfigType.NUMBER
        );
    }
    
    /**
     * 迁移默认密码配置
     */
    private void migrateDefaultPassword() {
        systemConfigService.setConfig(
            "student.default.password",
            "123456",
            "学生默认密码",
            SystemConfig.ConfigType.STRING
        );
    }
    
    /**
     * 迁移学生姓名库
     */
    private void migrateStudentNames() {
        String[] maleNames = {"张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十",
                "陈一", "刘二", "杨三", "黄四", "朱五", "林六", "何七", "郭八",
                "马九", "罗十", "梁一", "宋二", "唐三", "许四", "韩五", "冯六",
                "邓七", "曹八", "彭九", "曾十", "肖一", "田二", "董三", "袁四",
                "潘五", "于六", "蒋七", "蔡八", "余九", "杜十", "叶一", "程二",
                "苏三", "魏四", "吕五", "丁六", "任七", "沈八", "姚九", "卢十",
                "姜一", "崔二"};
        
        String[] femaleNames = {"小红", "小芳", "小丽", "小华", "小燕", "小玲", "小梅", "小霞",
                "小娟", "小敏", "小静", "小雪", "小兰", "小慧", "小琴", "小云",
                "小萍", "小英", "小莉", "小珍", "小艳", "小凤", "小秀", "小娜",
                "小婷", "小洁", "小倩", "小琳", "小欣", "小丹", "小蓉", "小佳",
                "小薇", "小晶", "小颖", "小璐", "小瑶", "小婷", "小雯", "小月",
                "小菲", "小娇", "小妮", "小茜", "小媛", "小露", "小曼", "小诗",
                "小韵", "小悦"};
        
        systemConfigService.setConfig(
            "student.names.male",
            String.join(",", maleNames),
            "男生姓名库",
            SystemConfig.ConfigType.ARRAY
        );
        
        systemConfigService.setConfig(
            "student.names.female",
            String.join(",", femaleNames),
            "女生姓名库",
            SystemConfig.ConfigType.ARRAY
        );
    }
    
    /**
     * 迁移电话号码生成规则
     */
    private void migratePhoneNumberRules() {
        systemConfigService.setConfig(
            "student.phone.prefix.male",
            "1380000",
            "男生电话号码前缀",
            SystemConfig.ConfigType.STRING
        );
        
        systemConfigService.setConfig(
            "student.phone.prefix.female",
            "1390000",
            "女生电话号码前缀",
            SystemConfig.ConfigType.STRING
        );
        
        systemConfigService.setConfig(
            "phone.validation.regex",
            "^1[3-9]\\d{9}$",
            "电话号码验证正则表达式",
            SystemConfig.ConfigType.STRING
        );
    }
}
