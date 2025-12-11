package org.delcom.app.controllers;

import jakarta.servlet.http.HttpSession;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.configs.AuthContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Tambahan
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
// HAPUS import csrf karena addFilters=false
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@WebMvcTest(WebAuthController.class)
// PENTING: addFilters = false mematikan Spring Security agar logika manual Controller bisa jalan
@AutoConfigureMockMvc(addFilters = false) 
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // Mock Bean tetap diperlukan agar Context tidak error saat load
    @MockitoBean
    private AuthTokenService authTokenService;

    @MockitoBean
    private AuthContext authContext;

    @Test
    void showLoginForm_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"));
    }

    @Test
    void processLogin_ValidCredentials_ShouldRedirectToPlantsAndSetSession() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("encoded");

        when(userService.getUserByEmail("user@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", "user@example.com")
                        .param("password", "pass")
                        // csrf() dihapus karena filter mati
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plants"))
                .andReturn();

        HttpSession session = result.getRequest().getSession();
        assertNotNull(session);
        assertNotNull(session.getAttribute("currentUser"));
        
        verify(userService, times(1)).getUserByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void processLogin_ValidationError_ShouldReturnLoginView() throws Exception {
        // Email kosong
        mockMvc.perform(post("/login")
                        .param("email", "") 
                        .param("password", "pass")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "email")); 
        
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void processLogin_UserNotFound_ShouldReturnLoginViewWithError() throws Exception {
        when(userService.getUserByEmail("unknown@example.com")).thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("email", "unknown@example.com")
                        .param("password", "pass")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "email")); 

        verify(userService, times(1)).getUserByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void processLogin_WrongPassword_ShouldReturnLoginViewWithError() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("encoded");

        when(userService.getUserByEmail("user@example.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        mockMvc.perform(post("/login")
                        .param("email", "user@example.com")
                        .param("password", "wrong")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "password")); 
        
        verify(userService, times(1)).getUserByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
    
    @Test
    void logout_ShouldInvalidateSessionAndRedirectToLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertNull(session, "Session seharusnya sudah di-invalidate");
    }
}