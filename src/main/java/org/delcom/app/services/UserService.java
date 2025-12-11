package org.delcom.app.services;

import java.util.Collections;
import java.util.UUID;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService { // <--- PENTING: Implement ini
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- LOGIC LOGIN SPRING SECURITY (WAJIB ADA) ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cari user berdasarkan email
        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan dengan email: " + email));

        // Kembalikan object User milik Spring Security (bukan Entity kita)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // Password hash dari DB
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // --- LOGIC CRUD BIASA ---
    @Transactional
    public User createUser(String name, String email, String password) {
        User user = new User(name, email, password);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findFirstByEmail(email).orElse(null);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User updateUser(UUID id, String name, String email) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        user.setPassword(newPassword);
        return userRepository.save(user);
    }
}