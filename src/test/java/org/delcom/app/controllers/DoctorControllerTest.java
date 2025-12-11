package org.delcom.app.controllers;

import org.delcom.app.entities.PlantHealth;
import org.delcom.app.entities.User;
import org.delcom.app.services.PlantHealthService;
import org.delcom.app.services.UserService;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.configs.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@WithMockUser(username = "testuser@delcom.org", roles = "DOCTOR")
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlantHealthService plantHealthService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @MockitoBean
    private AuthContext authContext;

    private User mockUser;
    private final UUID MOCK_USER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("testuser@delcom.org");
        mockUser.setId(MOCK_USER_UUID);
        when(userService.getUserByEmail("testuser@delcom.org")).thenReturn(mockUser);
    }

    @Test
    void listDoctor_ShouldReturnDoctorViewAndLogs() throws Exception {
        PlantHealth log = new PlantHealth();
        log.setId(1L);
        List<PlantHealth> mockLogs = Collections.singletonList(log);

        when(plantHealthService.getLogsByUser(mockUser)).thenReturn(mockLogs);

        mockMvc.perform(get("/doctor"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/doctor"))
                .andExpect(model().attributeExists("logs"));
    }

    @Test
    void newLog_ShouldReturnDoctorFormView() throws Exception {
        mockMvc.perform(get("/doctor/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/doctor-form"))
                .andExpect(model().attributeExists("plantHealth"));
    }

    @Test
    void saveLog_WhenSaveIsSuccessful_ShouldRedirectWithSuccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "filename.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes()
        );
        mockMvc.perform(multipart("/doctor/save")
                        .file(imageFile)
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor?success=Catatan berhasil disimpan"));
    }

    @Test
    void saveLog_WhenSaveFails_ShouldRedirectWithError() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "filename.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes()
        );
        
        // Menggunakan ArgumentMatchers secara eksplisit agar tidak perlu import static
        doThrow(new RuntimeException("Error")).when(plantHealthService)
            .saveLog(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(multipart("/doctor/save")
                        .file(imageFile)
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor?error=Gagal menyimpan data"));
    }

    @Test
    void editLog_WhenLogFound_ShouldReturnDoctorFormView() throws Exception {
        PlantHealth log = new PlantHealth();
        log.setId(1L);
        when(plantHealthService.getLogById(1L)).thenReturn(log);

        mockMvc.perform(get("/doctor/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/doctor-form"))
                .andExpect(model().attribute("plantHealth", log));
    }

    @Test
    void editLog_WhenLogNotFound_ShouldRedirectWithError() throws Exception {
        when(plantHealthService.getLogById(99L)).thenReturn(null);

        mockMvc.perform(get("/doctor/99/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor?error=Data tidak ditemukan"));
    }

    @Test
    void deleteLog_ShouldDeleteAndRedirectWithSuccess() throws Exception {
        mockMvc.perform(get("/doctor/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor?success=Catatan berhasil dihapus"));
    }
}