package org.example.bwxw.service;

import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(10)
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (User user : userRepository.findAll()) {
            String password = user.getPassword();
            if (password == null || password.isEmpty()) {
                continue;
            }
            boolean alreadyBcrypt = password.startsWith("$2a$")
                    || password.startsWith("$2b$")
                    || password.startsWith("$2y$");
            if (!alreadyBcrypt) {
                user.setPassword(passwordEncoder.encode(password));
                if (user.getRole() == User.UserRole.STUDENT && "123456".equals(password)) {
                    user.setMustChangePassword(true);
                }
                userRepository.save(user);
            }
        }
    }
}
