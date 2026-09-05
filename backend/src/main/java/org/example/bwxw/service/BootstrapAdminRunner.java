package org.example.bwxw.service;

import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class BootstrapAdminRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String name;

    public BootstrapAdminRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username:}") String username,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.name:系统管理员}") String name) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password;
        this.name = name == null || name.isBlank() ? "系统管理员" : name.trim();
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.countByRole(User.UserRole.ADMIN) > 0) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            log.warn("数据库中没有管理员。请设置 SMART_DORM_BOOTSTRAP_USERNAME 和 SMART_DORM_BOOTSTRAP_PASSWORD 后重启。");
            return;
        }
        if (username.length() < 3 || username.length() > 50 || password.length() < 8 || password.length() > 128) {
            throw new IllegalStateException("首个管理员用户名需为3至50位，临时密码需为8至128位");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("首个管理员用户名已被占用");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setName(name);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(User.UserRole.ADMIN);
        admin.setAdminType(User.AdminType.SUPER_ADMIN);
        admin.setStatus("ACTIVE");
        admin.setMustChangePassword(true);
        userRepository.save(admin);
        log.info("首个超级管理员已创建，请登录后立即修改临时密码。");
    }
}
