package org.delcom.app.controllers;

import org.delcom.app.entities.Plant;
import org.delcom.app.entities.User;
import org.delcom.app.services.PlantService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantControllerTest {

    @Mock
    private PlantService plantService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PlantController plantController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@mail.com");
    }

    // --- TEST 1: List Plants - Statistics & Logic Coverage ---
    @Test
    void testListPlants_Success_WithStats() {
        // Mock Security Static
        try (MockedStatic<SecurityContextHolder> securityMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("test@mail.com");

            when(userService.getUserByEmail("test@mail.com")).thenReturn(mockUser);

            // Setup Data Tanaman untuk Coverage Stream Logic
            // 1. Tanaman Normal, Butuh Air (Next watering < Today)
            Plant p1 = new Plant(); 
            p1.setLastWatered(LocalDate.now().minusDays(5));
            p1.setWateringFrequency(2); // Next: 3 days ago (Butuh Air)
            p1.setSpecies("Mawar");

            // 2. Tanaman Sehat (Next watering > Today)
            Plant p2 = new Plant();
            p2.setLastWatered(LocalDate.now());
            p2.setWateringFrequency(10); // Next: 10 days later (Sehat)
            p2.setSpecies("Melati");

            // 3. Tanaman Missing LastWatered (Logic check: return false)
            Plant p3 = new Plant();
            p3.setLastWatered(null);
            p3.setWateringFrequency(5);
            p3.setSpecies(null); // Species null -> mapped to "lainnya"

            // 4. Tanaman Missing Frequency (Logic check: return false)
            Plant p4 = new Plant();
            p4.setLastWatered(LocalDate.now());
            p4.setWateringFrequency(null);
            p4.setSpecies("Mawar"); // Duplicate species (case insensitive check)

            List<Plant> plants = Arrays.asList(p1, p2, p3, p4);
            when(plantService.getPlantsByUser(mockUser)).thenReturn(plants);

            // Execute
            String viewName = plantController.listPlants(model);

            // Verify View
            assertEquals("pages/plants/list", viewName);
            
            // Verifikasi Statistik:
            // totalPlants = 4
            verify(model).addAttribute("totalPlants", 4);
            
            // needsWater = 1 (Hanya p1. p3 & p4 gagal validasi null, p2 sehat)
            verify(model).addAttribute("needsWater", 1L);
            
            // healthy = Total (4) - NeedsWater (1) = 3
            verify(model).addAttribute("healthy", 3L);
            
            // uniqueSpecies = 3 ("mawar", "melati", "lainnya")
            verify(model).addAttribute("uniqueSpecies", 3L);
            
            verify(model).addAttribute(eq("plants"), anyList());
        }
    }

    // --- TEST 2: List Plants - Exception Handling (Catch Block) ---
    @Test
    void testListPlants_Exception() {
        try (MockedStatic<SecurityContextHolder> securityMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            // Force Exception saat ambil nama user
            when(authentication.getName()).thenThrow(new RuntimeException("DB Error"));

            String viewName = plantController.listPlants(model);

            assertEquals("pages/plants/list", viewName);
            // Verifikasi masuk blok catch dan return empty list
            verify(model).addAttribute(eq("plants"), eq(Collections.emptyList()));
        }
    }

    // --- TEST 3: Show Create Form ---
    @Test
    void testShowCreateForm() {
        String viewName = plantController.showCreateForm(model);
        assertEquals("pages/plants/form", viewName);
        verify(model).addAttribute(eq("plant"), any(Plant.class));
    }

    // --- TEST 4: Save Plant - Success ---
    @Test
    void testSavePlant_Success() throws Exception {
        try (MockedStatic<SecurityContextHolder> securityMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("test@mail.com");
            when(userService.getUserByEmail("test@mail.com")).thenReturn(mockUser);

            Plant plant = new Plant();
            String result = plantController.savePlant(plant, multipartFile);

            verify(plantService).savePlant(plant, multipartFile, mockUser);
            // Cek redirect success
            assertTrue(result.contains("success="));
        }
    }

    // --- TEST 5: Save Plant - Exception/Failure ---
    @Test
    void testSavePlant_Failure() throws Exception {
        try (MockedStatic<SecurityContextHolder> securityMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("test@mail.com");
            when(userService.getUserByEmail("test@mail.com")).thenReturn(mockUser);

            Plant plant = new Plant();
            // Force Exception di Service
            doThrow(new RuntimeException("IO Error")).when(plantService).savePlant(any(), any(), any());

            String result = plantController.savePlant(plant, multipartFile);

            // Cek redirect error
            assertTrue(result.contains("error="));
        }
    }

    // --- TEST 6: Detail Plant - Found ---
    @Test
    void testDetailPlant_Found() {
        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        when(plantService.getPlantById(id)).thenReturn(plant);

        String result = plantController.detailPlant(id, model);

        assertEquals("pages/plants/detail", result);
        verify(model).addAttribute("plant", plant);
    }

    // --- TEST 7: Detail Plant - Not Found ---
    @Test
    void testDetailPlant_NotFound() {
        UUID id = UUID.randomUUID();
        when(plantService.getPlantById(id)).thenReturn(null);

        String result = plantController.detailPlant(id, model);

        assertTrue(result.contains("error="));
    }

    // --- TEST 8: Show Edit Form - Found ---
    @Test
    void testShowEditForm_Found() {
        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        when(plantService.getPlantById(id)).thenReturn(plant);

        String result = plantController.showEditForm(id, model);

        assertEquals("pages/plants/form", result);
        verify(model).addAttribute("plant", plant);
    }

    // --- TEST 9: Show Edit Form - Not Found ---
    @Test
    void testShowEditForm_NotFound() {
        UUID id = UUID.randomUUID();
        when(plantService.getPlantById(id)).thenReturn(null);

        String result = plantController.showEditForm(id, model);

        assertTrue(result.contains("error="));
    }

    // --- TEST 10: Delete Plant ---
    @Test
    void testDeletePlant() {
        UUID id = UUID.randomUUID();
        String result = plantController.deletePlant(id);

        verify(plantService).deletePlant(id);
        assertTrue(result.contains("success="));
    }

    // --- TEST 11: Water Plant Now ---
    @Test
    void testWaterPlantNow() {
        UUID id = UUID.randomUUID();
        String result = plantController.waterPlantNow(id);

        verify(plantService).waterPlantNow(id);
        assertTrue(result.contains("success="));
    }
}