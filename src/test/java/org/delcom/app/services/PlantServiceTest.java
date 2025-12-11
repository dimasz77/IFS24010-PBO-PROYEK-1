package org.delcom.app.services;

import org.delcom.app.entities.Plant;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.PlantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    private PlantService plantService;

    // --- TEST 1: Constructor (Success) ---
    @Test
    void testConstructor_Success() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            
            plantService = new PlantService(plantRepository);
            
            filesMock.verify(() -> Files.createDirectories(any(Path.class)));
        }
    }

    // --- TEST 2: Constructor (Exception Catch) ---
    @Test
    void testConstructor_Exception() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Disk Error"));
            
            assertDoesNotThrow(() -> new PlantService(plantRepository));
        }
    }

    // --- TEST 3: Get Plants By User ---
    @Test
    void testGetPlantsByUser() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        User user = new User();
        when(plantRepository.findByUser(user)).thenReturn(Collections.emptyList());
        
        List<Plant> result = plantService.getPlantsByUser(user);
        assertNotNull(result);
        verify(plantRepository).findByUser(user);
    }

    // --- TEST 4: Get Plant By Id ---
    @Test
    void testGetPlantById() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        when(plantRepository.findById(id)).thenReturn(Optional.of(plant));
        Plant result = plantService.getPlantById(id);
        assertEquals(plant, result);
    }

    // --- TEST 5: Save Plant - File is NULL ---
    @Test
    void testSavePlant_FileIsNull() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        User user = new User();

        plantService.savePlant(plant, null, user);

        assertEquals(user, plant.getUser());
        verify(plantRepository).save(plant);
        assertNull(plant.getImagePath());
    }

    // --- TEST 6: Save Plant - File is EMPTY ---
    @Test
    void testSavePlant_FileIsEmpty() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        User user = new User();
        MultipartFile file = mock(MultipartFile.class);
        
        when(file.isEmpty()).thenReturn(true);

        plantService.savePlant(plant, file, user);

        verify(plantRepository).save(plant);
        verify(file).isEmpty();
    }

    // --- TEST 7: Save Plant - With New File ---
    @Test
    void testSavePlant_WithNewFile() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("bunga.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);

            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);

            plantService.savePlant(plant, file, user);

            assertNotNull(plant.getImagePath());
            assertTrue(plant.getImagePath().contains("bunga.jpg"));
            filesMock.verify(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)));
            verify(plantRepository).save(plant);
        }
    }

    // --- TEST 8: Save Plant - Replace Old File ---
    @Test
    void testSavePlant_ReplaceOldFile() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        plant.setImagePath("old-image.jpg"); 
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new-image.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {

            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);

            plantService.savePlant(plant, file, user);

            filesMock.verify(() -> Files.deleteIfExists(any(Path.class))); 
            assertTrue(plant.getImagePath().contains("new-image.jpg"));
        }
    }

    // --- TEST 9: Save Plant - Old Image is Empty String ---
    @Test
    void testSavePlant_OldImageIsEmptyString() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        plant.setImagePath(""); 
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {

            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);

            plantService.savePlant(plant, file, user);

            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)), never());
            assertTrue(plant.getImagePath().contains("new.jpg"));
        }
    }

    // --- TEST 10: Save Plant - Same Filename (Coverage Line 40) ---
    @Test
    void testSavePlant_SameFilename_UUIDMock() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        User user = new User();
        
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String originalFilename = "test.jpg";
        String expectedFilename = fixedUuid.toString() + "_" + originalFilename;

        plant.setImagePath(expectedFilename);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        // PENTING: Gunakan CALLS_REAL_METHODS pada UUID agar toString() berfungsi normal
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class);
             MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {
            
            uuidMock.when(UUID::randomUUID).thenReturn(fixedUuid);

            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);

            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);

            plantService.savePlant(plant, file, user);

            // VERIFY: never() karena logika code: if (!equals) delete.
            // Karena equals == true, maka delete di-skip.
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)), never());
            
            assertEquals(expectedFilename, plant.getImagePath());
        }
    }

    // --- TEST 11: Save Plant - Exception Ignored (Coverage Line 41) ---
    @Test
    void testSavePlant_ReplaceOldFile_Exception() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        plant.setImagePath("old.jpg");
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);

            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Locked"));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);
            
            assertDoesNotThrow(() -> plantService.savePlant(plant, file, user));
        }
    }

    // --- TEST 12: Delete Plant - Not Found ---
    @Test
    void testDeletePlant_NotFound() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }
        UUID id = UUID.randomUUID();
        when(plantRepository.findById(id)).thenReturn(Optional.empty());
        plantService.deletePlant(id);
        verify(plantRepository, never()).deleteById(id);
    }

    // --- TEST 13: Delete Plant - Success ---
    @Test
    void testDeletePlant_SuccessWithFile() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        plant.setImagePath("img.jpg");
        when(plantRepository.findById(id)).thenReturn(Optional.of(plant));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);

            plantService.deletePlant(id);
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)));
            verify(plantRepository).deleteById(id);
        }
    }

    // --- TEST 14: Delete Plant - Exception Ignored (Coverage Line 53) ---
    @Test
    void testDeletePlant_Exception() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        plant.setImagePath("img.jpg");
        when(plantRepository.findById(id)).thenReturn(Optional.of(plant));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            Path mockPath = mock(Path.class);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mockPath);

            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Error"));
            plantService.deletePlant(id);
            verify(plantRepository).deleteById(id);
        }
    }

    // --- TEST 15: Water Plant - Not Found ---
    @Test
    void testWaterPlantNow_NotFound() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }
        UUID id = UUID.randomUUID();
        when(plantRepository.findById(id)).thenReturn(Optional.empty());
        plantService.waterPlantNow(id);
        verify(plantRepository, never()).save(any());
    }

    // --- TEST 16: Water Plant - Success ---
    @Test
    void testWaterPlantNow_Success() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }
        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        when(plantRepository.findById(id)).thenReturn(Optional.of(plant));
        plantService.waterPlantNow(id);
        assertEquals(LocalDate.now(), plant.getLastWatered());
        verify(plantRepository).save(plant);
    }

    // --- TEST 17: Save Plant - Date Already Exists (Coverage Line 45: ELSE branch) ---
    @Test
    void testSavePlant_DateAlreadyExists() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        Plant plant = new Plant();
        LocalDate oldDate = LocalDate.of(2020, 1, 1);
        plant.setLastWatered(oldDate); // Set existing date
        User user = new User();

        plantService.savePlant(plant, null, user);

        // Verify date NOT changed to now()
        assertEquals(oldDate, plant.getLastWatered());
        verify(plantRepository).save(plant);
    }

    // --- TEST 18: Delete Plant - No Image (Coverage Line 52: ELSE branch) ---
    @Test
    void testDeletePlant_NoImage() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            plantService = new PlantService(plantRepository);
        }

        UUID id = UUID.randomUUID();
        Plant plant = new Plant();
        plant.setImagePath(null); // No image
        when(plantRepository.findById(id)).thenReturn(Optional.of(plant));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            plantService.deletePlant(id);
            
            // Verify delete logic SKIPPED
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            verify(plantRepository).deleteById(id);
        }
    }
}