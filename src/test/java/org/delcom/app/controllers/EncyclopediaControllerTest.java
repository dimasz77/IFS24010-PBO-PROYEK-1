package org.delcom.app.controllers;

import org.delcom.app.services.EncyclopediaService;
import org.delcom.app.services.UserService;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.configs.AuthContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EncyclopediaController.class)
@WithMockUser
class EncyclopediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EncyclopediaService encyclopediaService;

    // WAJIB: Mock Interceptor Dependencies
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthTokenService authTokenService;
    @MockitoBean
    private AuthContext authContext;

    @Test
    void listEntries_ShouldReturnEncyclopediaViewAndHardcodedTips() throws Exception {
        // Karena data di Controller hardcoded (new ArrayList), kita tidak perlu mock return value service
        mockMvc.perform(get("/encyclopedia"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/encyclopedia"))
                .andExpect(model().attributeExists("entries"));
    }
}