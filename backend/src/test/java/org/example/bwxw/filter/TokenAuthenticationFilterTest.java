package org.example.bwxw.filter;

import jakarta.servlet.FilterChain;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter filter;

    @Test
    void permitsReadsInStudentVisitorMode() throws Exception {
        String token = "student-read-token";
        User user = forcedUser();
        TokenAuthenticationFilter.storeToken(token, user.getStudentId());
        when(userRepository.findByStudentId(user.getStudentId())).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/dashboard/stats");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        } finally {
            TokenAuthenticationFilter.removeToken(token);
        }
    }

    @Test
    void blocksWritesInStudentVisitorMode() throws Exception {
        String token = "student-write-token";
        User user = forcedUser();
        TokenAuthenticationFilter.storeToken(token, user.getStudentId());
        when(userRepository.findByStudentId(user.getStudentId())).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/repairs");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, filterChain);
            assertEquals(403, response.getStatus());
            assertTrue(response.getContentAsString().contains("只读访客模式"));
            verify(filterChain, never()).doFilter(request, response);
        } finally {
            TokenAuthenticationFilter.removeToken(token);
        }
    }

    @Test
    void permitsPasswordChangeInStudentVisitorMode() throws Exception {
        String token = "student-password-token";
        User user = forcedUser();
        TokenAuthenticationFilter.storeToken(token, user.getStudentId());
        when(userRepository.findByStudentId(user.getStudentId())).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/users/password");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        } finally {
            TokenAuthenticationFilter.removeToken(token);
        }
    }

    private User forcedUser() {
        User user = new User();
        user.setStudentId("20240001");
        user.setUsername("20240001");
        user.setRole(User.UserRole.STUDENT);
        user.setMustChangePassword(true);
        return user;
    }
}
