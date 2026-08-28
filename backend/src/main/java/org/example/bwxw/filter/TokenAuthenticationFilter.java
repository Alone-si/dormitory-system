package org.example.bwxw.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserRepository userRepository;
    
    private static final long TOKEN_TTL_MILLIS = 2 * 60 * 60 * 1000L;
    private static final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    public static void storeToken(String token, String userIdentifier) {
        tokenStore.put(token, new TokenEntry(userIdentifier, System.currentTimeMillis() + TOKEN_TTL_MILLIS));
    }

    public static void removeToken(String token) {
        tokenStore.remove(token);
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            TokenEntry entry = tokenStore.get(token);
            if (entry == null || entry.isExpired()) {
                tokenStore.remove(token);
            } else {
                try {
                    String userIdentifier = entry.getUserIdentifier();
                    User user = userRepository.findByStudentId(userIdentifier)
                            .or(() -> userRepository.findByUsername(userIdentifier))
                            .orElse(null);

                    if (user != null) {
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                        if (user.getAdminType() == User.AdminType.SUPER_ADMIN) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                        }

                        String principal = user.getStudentId() != null && !user.getStudentId().isEmpty()
                                ? user.getStudentId()
                                : user.getUsername();

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (user.getRole() == User.UserRole.STUDENT
                                && Boolean.TRUE.equals(user.getMustChangePassword())
                                && !isReadOnlyRequest(request)) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"code\":403,\"message\":\"当前为只读访客模式，修改密码后即可操作\",\"data\":null}"
                            );
                            return;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isReadOnlyRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        String path = request.getRequestURI();
        return "/api/users/password".equals(path) || "/api/auth/logout".equals(path);
    }

    private static class TokenEntry {
        private final String userIdentifier;
        private final long expiresAt;

        TokenEntry(String userIdentifier, long expiresAt) {
            this.userIdentifier = userIdentifier;
            this.expiresAt = expiresAt;
        }

        String getUserIdentifier() {
            return userIdentifier;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
