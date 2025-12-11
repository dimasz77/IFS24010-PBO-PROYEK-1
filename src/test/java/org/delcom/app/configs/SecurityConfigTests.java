package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.delcom.app.services.PlantHealthService;
import org.delcom.app.services.PlantService;
import org.delcom.app.services.ScheduleService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
// import anyString dihapus karena tidak terpakai
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PlantService plantService;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private PlantHealthService plantHealthService;

    @Test
    void testBeansExist() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        assertNotNull(provider);
    }

    @Test
    void testPublicEndpoint_LoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testPublicEndpoint_Assets() throws Exception {
        mockMvc.perform(get("/assets/css/style.css"))
                .andExpect(status().isNotFound()); 
    }

    @Test
    void testProtectedEndpoint_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    @Test
    @WithMockUser(username = "testuser") 
    void testProtectedEndpoint_AuthenticatedUser_AccessGranted() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("testuser");
        mockUser.setName("Test User");
        when(userService.getUserByEmail("testuser")).thenReturn(mockUser);
        
        when(plantService.getPlantsByUser(any())).thenReturn(Collections.emptyList());
        when(scheduleService.getSchedulesByUser(any())).thenReturn(Collections.emptyList());
        when(plantHealthService.getLogsByUser(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testFormLoginConfig() throws Exception {
        mockMvc.perform(formLogin("/login").user("email", "random").password("fail"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=true"));
    }

    @Test
    void testLogoutConfig() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?logout=true"))
                .andExpect(unauthenticated());
    }
}