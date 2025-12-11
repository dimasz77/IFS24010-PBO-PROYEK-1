package org.delcom.app.views;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthView {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // LOGIN PAGE
    // ==========================================
    @GetMapping("/login")
    public String showLogin(Model model) {
        // Jika user sudah login, redirect ke Home
        if (isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("loginForm", new LoginForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    // Catatan: Kita TIDAK butuh @PostMapping("/login") di sini
    // Karena proses login ditangani otomatis oleh Spring Security
    // sesuai konfigurasi di SecurityConfig (.loginProcessingUrl("/login"))

    // ==========================================
    // REGISTER PAGE
    // ==========================================
    @GetMapping("/register")
    public String showRegister(Model model) {
        // Jika user sudah login, redirect ke Home
        if (isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("registerForm", new RegisterForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        // 1. Validasi Form Standard (Kosong/Format Email salah)
        if (bindingResult.hasErrors()) {
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // 2. Validasi Password Match (Manual check jika ada confirm password, di sini kita skip dulu)

        // 3. Cek apakah email sudah terpakai di DB
        User existingUser = userService.getUserByEmail(registerForm.getEmail());
        if (existingUser != null) {
            bindingResult.rejectValue("email", "error.registerForm", "Email ini sudah terdaftar. Silakan login.");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // 4. Enkripsi Password sebelum simpan
        String hashPassword = passwordEncoder.encode(registerForm.getPassword());

        // 5. Simpan User Baru
        try {
            userService.createUser(
                    registerForm.getName(),
                    registerForm.getEmail(),
                    hashPassword
            );
        } catch (Exception e) {
            bindingResult.rejectValue("email", "error.registerForm", "Gagal menyimpan data ke database.");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // 6. Sukses -> Redirect ke Login dengan pesan
        redirectAttributes.addFlashAttribute("success", "Registrasi Berhasil! Silakan Masuk.");
        return "redirect:/auth/login?registered=true";
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate session manual (opsional, karena SecurityConfig sudah handle)
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }

    // ==========================================
    // HELPER
    // ==========================================
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication instanceof AnonymousAuthenticationToken);
    }
}