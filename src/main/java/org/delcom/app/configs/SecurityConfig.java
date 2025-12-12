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

    // Inject UserService via Constructor
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Matikan CSRF sementara
            .authorizeHttpRequests(auth -> auth
                // Izinkan folder assets (css/js/images) dan endpoint auth serta H2 Console
                // Menambahkan path static yang umum agar layout tidak pecah
                .requestMatchers("/assets/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/auth/**", "/error", "/h2-console/**").permitAll()
                
                // Semua request lainnya WAJIB login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")       // Halaman HTML Login kustom
                .loginProcessingUrl("/login")   // URL Action form login
                .usernameParameter("email")     // PENTING: Login pakai Email
                .defaultSuccessUrl("/", true)   // Redirect ke Home setelah sukses
                .failureUrl("/auth/login?error=true") // Redirect jika gagal
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            )
            // Khusus H2 Console agar tidak blank (Allow Frames)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // Bean untuk menghubungkan Security dengan Database (UserService)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        /* 
           PERBAIKAN DISINI:
           Di Spring Boot versi baru, kita TIDAK BOLEH menggunakan:
           new DaoAuthenticationProvider() + authProvider.setUserDetailsService(...)
           
           GANTINYA:
           Masukkan userService langsung ke dalam kurung (Constructor Injection).
        */
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService);
        
        // Set PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder()); 
        
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}