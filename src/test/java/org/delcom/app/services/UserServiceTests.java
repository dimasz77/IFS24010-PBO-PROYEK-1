package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- 1. Test loadUserByUsername (Spring Security) ---

    @Test
    void testLoadUserByUsername_Success() {
        String email = "test@example.com";
        String password = "hashedpassword";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);

        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        // Memastikan role di-set secara hardcode "ROLE_USER" sesuai kode Anda
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_NotFound_ThrowsException() {
        String email = "unknown@example.com";
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
    }

    // --- 2. Test createUser ---

    @Test
    void testCreateUser() {
        String name = "John";
        String email = "john@example.com";
        String password = "secret";

        User savedUser = new User(name, email, password);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(name, email, password);

        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(userRepository).save(any(User.class));
    }

    // --- 3. Test getUserByEmail ---

    @Test
    void testGetUserByEmail_Found() {
        String email = "exist@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        String email = "ghost@example.com";
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.empty());

        User result = userService.getUserByEmail(email);

        assertNull(result); // Karena .orElse(null)
    }

    // --- 4. Test getUserById ---

    @Test
    void testGetUserById_Found() {
        UUID id = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.getUserById(id);

        assertNull(result); // Karena .orElse(null)
    }

    // --- 5. Test updateUser ---

    @Test
    void testUpdateUser_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@email.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(id, "New Name", "new@email.com");

        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("new@email.com", updated.getEmail());
    }

    @Test
    void testUpdateUser_NotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Ini mengetes baris: if (user == null) return null;
        User result = userService.updateUser(id, "Name", "email");

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    // --- 6. Test updatePassword ---

    @Test
    void testUpdatePassword_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setPassword("oldPass");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updatePassword(id, "newPass123");

        assertNotNull(updated);
        assertEquals("newPass123", updated.getPassword());
    }

    @Test
    void testUpdatePassword_NotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Ini mengetes baris: if (user == null) return null;
        User result = userService.updatePassword(id, "newPass");

        assertNull(result);
        verify(userRepository, never()).save(any());
    }
}