package org.example.bwxw.service;

import org.example.bwxw.dto.LoginRequest;
import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void rejectsBlankCredentials() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(new LoginRequest())
        );

        assertEquals("用户名和密码不能为空", error.getMessage());
    }

    @Test
    void hidesWhetherAccountExists() {
        LoginRequest request = loginRequest("missing", "wrong-password");
        when(userRepository.findByStudentId("missing")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("用户名或密码错误", error.getMessage());
    }

    @Test
    void locksAccountAfterFiveFailedAttempts() {
        User user = new User();
        user.setPassword("encoded");
        user.setStatus("ACTIVE");
        LoginRequest request = loginRequest("student", "wrong-password");
        when(userRepository.findByStudentId("student")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        }

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> authService.login(request)
        );
        assertEquals("登录失败次数过多，请5分钟后再试", error.getMessage());
    }

    @Test
    void doesNotForceAdminToChangeDefaultPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setName("管理员");
        user.setRole(User.UserRole.ADMIN);
        user.setPassword("encoded");
        user.setStatus("ACTIVE");
        user.setMustChangePassword(true);
        LoginRequest request = loginRequest("admin", "123456");
        when(userRepository.findByStudentId("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);

        authService.login(request);

        assertEquals(false, user.getMustChangePassword());
        verify(userRepository).save(user);
    }

    @Test
    void marksStudentUsingDefaultPasswordAsReadOnly() {
        User user = new User();
        user.setId(2L);
        user.setUsername("20240001");
        user.setStudentId("20240001");
        user.setName("学生");
        user.setRole(User.UserRole.STUDENT);
        user.setPassword("encoded");
        user.setStatus("ACTIVE");
        LoginRequest request = loginRequest("20240001", "123456");
        when(userRepository.findByStudentId("20240001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);

        authService.login(request);

        assertEquals(true, user.getMustChangePassword());
        verify(userRepository).save(user);
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
