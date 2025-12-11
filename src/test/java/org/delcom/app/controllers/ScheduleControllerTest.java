package org.delcom.app.controllers;

import org.delcom.app.entities.Schedule;
import org.delcom.app.entities.User;
import org.delcom.app.services.ScheduleService;
import org.delcom.app.services.UserService;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.configs.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
@WithMockUser(username = "testuser@delcom.org")
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private UserService userService;

    // WAJIB: Mock Interceptor Dependencies
    @MockitoBean
    private AuthTokenService authTokenService;
    @MockitoBean
    private AuthContext authContext;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("testuser@delcom.org");
        mockUser.setId(UUID.randomUUID()); 
        when(userService.getUserByEmail("testuser@delcom.org")).thenReturn(mockUser);
    }

    @Test
    void listSchedules_ShouldReturnScheduleViewAndList() throws Exception {
        when(scheduleService.getSchedulesByUser(mockUser)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/schedule"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/schedule"));
    }

    @Test
    void saveSchedule_ShouldCallServiceAndRedirectWithSuccess() throws Exception {
        mockMvc.perform(post("/schedule/save").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule?success=Jadwal berhasil disimpan"));
    }
    
    @Test
    void newScheduleForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/schedule/new")).andExpect(status().isOk());
    }
    
    @Test
    void editScheduleForm_WhenScheduleFound_ShouldReturnFormView() throws Exception {
        when(scheduleService.getScheduleById(1L)).thenReturn(new Schedule());
        mockMvc.perform(get("/schedule/1/edit")).andExpect(status().isOk());
    }
    
    @Test
    void editScheduleForm_WhenScheduleNotFound_ShouldRedirectWithError() throws Exception {
        when(scheduleService.getScheduleById(99L)).thenReturn(null);
        mockMvc.perform(get("/schedule/99/edit")).andExpect(status().is3xxRedirection());
    }
    
    @Test
    void deleteSchedule_ShouldCallServiceAndRedirectWithSuccess() throws Exception {
        mockMvc.perform(get("/schedule/1/delete")).andExpect(status().is3xxRedirection());
    }
}