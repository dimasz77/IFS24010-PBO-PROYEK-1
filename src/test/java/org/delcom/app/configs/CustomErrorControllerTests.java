package org.delcom.app.configs;

import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomErrorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ErrorAttributes errorAttributes;

    // --- TAMBAHAN MOCK AGAR CONTEXT TIDAK CRASH (KARENA INTERCEPTOR) ---
    @MockitoBean
    private AuthTokenService authTokenService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthContext authContext;
    // -------------------------------------------------------------------

    @Test
    void handleError_ShouldReturnApiResponseWithDetails() throws Exception {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", "Simulated Error");
        errorMap.put("path", "/api/test");
        errorMap.put("status", 404);
        errorMap.put("error", "Not Found");

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(errorMap);

        mockMvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 404))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("Simulated Error"));
    }
    
    @Test
    void handleError_WhenNoAttributes_ShouldReturnDefaultValues() throws Exception {
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan pada server"));
    }
}