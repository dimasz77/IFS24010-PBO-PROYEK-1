package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah user admin sudah ada
        if (userRepository.findFirstByEmail("admin@admin.com").isEmpty()) {
            
            // Buat user manual dengan password yang PASTI BENAR
            User admin = new User();
            admin.setName("Admin Ganteng");
            admin.setEmail("admin@admin.com");
            // Kita enkripsi password "admin123"
            admin.setPassword(passwordEncoder.encode("admin123")); 
            
            userRepository.save(admin);

            System.out.println("---------------------------------------------");
            System.out.println("✅ USER OTOMATIS BERHASIL DIBUAT!");
            System.out.println("📧 Email:    admin@admin.com");
            System.out.println("🔑 Password: admin123");
            System.out.println("---------------------------------------------");
        }
    }
}