package org.example.bwxw.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库检测服务 - 启动时检查数据库是否存在
 */
@Service
@Order(1) // 最先执行
public class DatabaseCheckService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCheckService.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) throws Exception {
        checkDatabase();
    }
    
    /**
     * 检查数据库连接
     */
    private void checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            String databaseName = connection.getCatalog();
            logger.info("=================================================");
            logger.info("数据库连接成功！");
            logger.info("数据库名称: {}", databaseName);
            logger.info("=================================================");
        } catch (SQLException e) {
            // 数据库不存在或连接失败
            String errorMessage = e.getMessage();
            
            if (errorMessage.contains("Unknown database")) {
                // 数据库不存在
                logger.error("=================================================");
                logger.error("❌ 数据库连接失败！");
                logger.error("");
                logger.error("错误原因：数据库 'alone' 不存在");
                logger.error("");
                logger.error("解决方案：");
                logger.error("1. 打开 MySQL 客户端（Navicat 或命令行）");
                logger.error("2. 执行以下 SQL 命令创建数据库：");
                logger.error("");
                logger.error("   CREATE DATABASE alone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
                logger.error("");
                logger.error("3. 重新启动后端服务");
                logger.error("");
                logger.error("注意：数据库表结构会自动创建，无需手动建表");
                logger.error("=================================================");
            } else if (errorMessage.contains("Access denied")) {
                // 密码错误
                logger.error("=================================================");
                logger.error("❌ 数据库连接失败！");
                logger.error("");
                logger.error("错误原因：用户名或密码错误");
                logger.error("");
                logger.error("解决方案：");
                logger.error("1. 检查 application.properties 中的配置：");
                logger.error("   spring.datasource.username=root");
                logger.error("   spring.datasource.password=你的MySQL密码");
                logger.error("");
                logger.error("2. 确认 MySQL 用户名和密码是否正确");
                logger.error("3. 修改配置后重新启动");
                logger.error("=================================================");
            } else {
                // 其他错误
                logger.error("=================================================");
                logger.error("❌ 数据库连接失败！");
                logger.error("");
                logger.error("错误信息：{}", errorMessage);
                logger.error("");
                logger.error("解决方案：");
                logger.error("1. 确认 MySQL 服务是否启动");
                logger.error("2. 检查 application.properties 中的数据库配置");
                logger.error("3. 确认端口 3306 未被占用");
                logger.error("=================================================");
            }
            
            // 抛出异常，阻止应用继续启动
            throw new RuntimeException("数据库连接失败，请按照上述提示解决问题后重新启动", e);
        }
    }
}
