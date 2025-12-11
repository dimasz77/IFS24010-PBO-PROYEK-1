package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
// IMPORT 'anyString' SUDAH DIHAPUS
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void run_WhenAdminDoesNotExist_ShouldCreateAdmin() throws Exception {
        when(userRepository.findFirstByEmail("admin@admin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("hashedPassword");

        dataSeeder.run();

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("admin123");
    }

    @Test
    void run_WhenAdminAlreadyExists_ShouldDoNothing() throws Exception {
        when(userRepository.findFirstByEmail("admin@admin.com")).thenReturn(Optional.of(new User()));

        dataSeeder.run();

        verify(userRepository, never()).save(any(User.class));
    }
}