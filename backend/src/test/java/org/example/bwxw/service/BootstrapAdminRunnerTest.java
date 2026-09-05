package org.example.bwxw.service;

import org.example.bwxw.entity.User;
import org.example.bwxw.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapAdminRunnerTest {

    @Test
    void createsFirstAdminWithForcedPasswordChange() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.countByRole(User.UserRole.ADMIN)).thenReturn(0L);
        when(repository.existsByUsername("owner")).thenReturn(false);
        when(encoder.encode("temporary-password")).thenReturn("encoded");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new BootstrapAdminRunner(repository, encoder, "owner", "temporary-password", "管理员").run();

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(admin -> {
            assertEquals(User.UserRole.ADMIN, admin.getRole());
            assertEquals(User.AdminType.SUPER_ADMIN, admin.getAdminType());
            assertEquals(true, admin.getMustChangePassword());
            assertEquals("encoded", admin.getPassword());
            return true;
        }));
    }
}
