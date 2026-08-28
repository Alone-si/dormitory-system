package org.example.bwxw.service;

import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("用户未登录");
        }

        String identifier = authentication.getName();
        return userRepository.findByStudentId(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new AccessDeniedException("用户不存在"));
    }
}
