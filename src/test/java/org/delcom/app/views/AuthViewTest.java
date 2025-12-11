package org.delcom.app.views;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthViewTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Model model;
    @Mock private BindingResult bindingResult;
    @Mock private RedirectAttributes redirectAttributes;
    @Mock private HttpSession session;
    
    // Mock Security Objects
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AuthView authView;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    // --- Helper Methods ---
    
    private void mockAuthenticatedUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(auth);
    }

    private void mockAnonymousUser() {
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
    }

    private void mockNullAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
    }

    // --- TESTS ---

    @Test
    void testShowLogin_WhenAuthenticated_RedirectsHome() {
        mockAuthenticatedUser(); 

        String view = authView.showLogin(model);

        assertEquals("redirect:/", view);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void testShowLogin_WhenAnonymous_ReturnsLoginForm() {
        mockAnonymousUser(); 

        String view = authView.showLogin(model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(model).addAttribute(eq("loginForm"), any(LoginForm.class));
    }

    @Test
    void testShowLogin_WhenAuthIsNull_ReturnsLoginForm() {
        mockNullAuthentication();

        String view = authView.showLogin(model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void testShowRegister_WhenAuthenticated_RedirectsHome() {
        mockAuthenticatedUser();

        String view = authView.showRegister(model);

        assertEquals("redirect:/", view);
    }

    @Test
    void testShowRegister_WhenAnonymous_ReturnsRegisterForm() {
        mockAnonymousUser();

        String view = authView.showRegister(model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(model).addAttribute(eq("registerForm"), any(RegisterForm.class));
    }

    // --- Process Register Tests ---

    @Test
    void testProcessRegister_ValidationErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        RegisterForm form = mock(RegisterForm.class);

        String view = authView.processRegister(form, bindingResult, redirectAttributes, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessRegister_EmailAlreadyExists() {
        RegisterForm form = mock(RegisterForm.class);
        when(form.getEmail()).thenReturn("exist@mail.com");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("exist@mail.com")).thenReturn(new User());

        String view = authView.processRegister(form, bindingResult, redirectAttributes, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    void testProcessRegister_ExceptionDuringSave() {
        RegisterForm form = mock(RegisterForm.class);
        when(form.getName()).thenReturn("Test");
        when(form.getEmail()).thenReturn("new@mail.com");
        when(form.getPassword()).thenReturn("pass");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("hashedPass");
        
        doThrow(new RuntimeException("DB Error")).when(userService).createUser(anyString(), anyString(), anyString());

        String view = authView.processRegister(form, bindingResult, redirectAttributes, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
    }

    @Test
    void testProcessRegister_Success() {
        RegisterForm form = mock(RegisterForm.class);
        when(form.getName()).thenReturn("User");
        when(form.getEmail()).thenReturn("valid@mail.com");
        when(form.getPassword()).thenReturn("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("valid@mail.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hashedXXX");

        String view = authView.processRegister(form, bindingResult, redirectAttributes, model);

        assertEquals("redirect:/auth/login?registered=true", view);
        verify(userService).createUser("User", "valid@mail.com", "hashedXXX");
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    @Test
    void testLogout() {
        String view = authView.logout(session);

        assertEquals("redirect:/auth/login?logout=true", view);
        verify(session).invalidate();
    }
}