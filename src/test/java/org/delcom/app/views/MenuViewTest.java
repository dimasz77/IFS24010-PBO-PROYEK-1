package org.delcom.app.views;

import org.delcom.app.entities.User;
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
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuViewTests {

    @InjectMocks
    private MenuView menuView;

    @Mock
    private Model model;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    // --- TEST 1: SUKSES LOGIN (Happy Path) ---
    @Test
    void testMenu_Success() {
        // Kita gunakan try-with-resources untuk mock static SecurityContextHolder
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            
            // Setup perilaku SecurityContextHolder
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            // Setup principal adalah User yang valid
            when(authentication.getPrincipal()).thenReturn(user);

            // Eksekusi
            String viewName = menuView.menu(model);

            // Verifikasi
            assertEquals("models/home", viewName);
            verify(model).addAttribute("auth", user);
        }
    }

    // --- TEST 2: BELUM LOGIN (Authentication Null) ---
    @Test
    void testMenu_NotLoggedIn_NullAuth() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null); // Auth null

            // Eksekusi
            String viewName = menuView.menu(model);

            // Verifikasi redirect
            assertEquals("redirect:/auth/logout", viewName);
            verifyNoInteractions(model);
        }
    }

    // --- TEST 3: USER ANONYMOUS (Belum Login tapi ada token anonim) ---
    @Test
    void testMenu_AnonymousUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            
            Authentication anonAuth = mock(AnonymousAuthenticationToken.class);
            
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(anonAuth); // Auth Anonymous

            String viewName = menuView.menu(model);

            assertEquals("redirect:/auth/logout", viewName);
        }
    }

    // --- TEST 4: TIPE PRINCIPAL SALAH (Bukan Object User) ---
    @Test
    void testMenu_WrongPrincipalType() {
        try (MockedStatic<SecurityContextHolder> mockedSecurity = Mockito.mockStatic(SecurityContextHolder.class)) {
            
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            // Principal mengembalikan String, bukan User
            when(authentication.getPrincipal()).thenReturn("Bukan User Object");

            String viewName = menuView.menu(model);

            assertEquals("redirect:/auth/logout", viewName);
        }
    }
}