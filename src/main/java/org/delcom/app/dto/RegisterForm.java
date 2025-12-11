package org.delcom.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Nama harus diisi")
    private String name; // <--- PENTING: Namanya 'name'

    @NotBlank(message = "Email harus diisi")
    @Email(message = "Format email tidak valid")
    private String email; // <--- PENTING: Namanya 'email'

    @NotBlank(message = "Password harus diisi")
    @Size(min = 3, message = "Password minimal 3 karakter")
    private String password;

    public RegisterForm() {}

    // Getter Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}