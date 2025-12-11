package org.delcom.app.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.delcom.app.dto.LoginForm; // Pastikan import LoginForm yang tadi
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder; // Gunakan Bean, jangan new manual
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // Inject PasswordEncoder (pastikan sudah ada @Bean di SecurityConfig)
    public WebAuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. TAMPILKAN HALAMAN LOGIN
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "pages/auth/login"; // Pastikan file html ada di folder ini
    }

    // 2. PROSES LOGIN (INI YANG PENTING)
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                               BindingResult result,
                               HttpSession session,
                               Model model) {

        // Cek validasi standar (email kosong, dll)
        if (result.hasErrors()) {
            return "pages/auth/login";
        }

        // --- MULAI LOGIKA CEK USER ---
        
        // Cari user berdasarkan email
        User user = userService.getUserByEmail(loginForm.getEmail());

        // A. Cek apakah user ada?
        if (user == null) {
            result.rejectValue("email", "error.user", "Email tidak terdaftar.");
            return "pages/auth/login";
        }

        // B. Cek apakah password cocok?
        // Gunakan passwordEncoder.matches(inputan_user, password_di_db)
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            result.rejectValue("password", "error.password", "Kata sandi salah.");
            return "pages/auth/login";
        }

        // --- LOGIN SUKSES ---
        
        // Simpan user ke Session (agar dianggap "Login")
        // Di aplikasi sederhana tanpa Spring Security penuh, kita pakai HttpSession
        session.setAttribute("currentUser", user);

        // Redirect ke halaman Plants
        return "redirect:/plants";
    }

    // 3. LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Hapus session
        return "redirect:/login";
    }
}
