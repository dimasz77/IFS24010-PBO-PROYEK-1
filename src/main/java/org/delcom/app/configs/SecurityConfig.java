package org.delcom.app.configs;

import org.delcom.app.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    // Inject UserService agar bisa dipakai oleh Security
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Matikan CSRF sementara
            .authorizeHttpRequests(auth -> auth
                // Izinkan folder assets (css/js/images) dan endpoint auth
                .requestMatchers("/assets/**", "/auth/**", "/error", "/h2-console/**").permitAll()
                
                // Semua request lainnya WAJIB login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")       // Halaman HTML Login kustom kita
                .loginProcessingUrl("/login")     // URL POST default Spring Security (sesuai form th:action)
                .usernameParameter("email")       // PENTING: Karena kita login pakai Email, bukan Username
                .defaultSuccessUrl("/", true)     // Redirect ke Home setelah sukses
                .failureUrl("/auth/login?error=true") // Redirect jika gagal
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            )
            // Khusus H2 Console agar tidak blank
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // Bean untuk menghubungkan Security dengan Database (UserService)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // ✅ PERBAIKAN UTAMA DISINI:
        // Gunakan konstruktor default (tanpa argumen)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // 1. Set UserDetailsService (yang diimplementasikan oleh userService)
        authProvider.setUserDetailsService(userService); 
        
        // 2. Set PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder()); // Gunakan BCrypt
        
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}