package org.delcom.app.services;

import org.delcom.app.entities.PlantHealth;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.PlantHealthRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantHealthServiceTest {

    @Mock
    private PlantHealthRepository repository;

    private PlantHealthService service;

    // --- TEST 1: Constructor Success (Directory Creation) ---
    @Test
    void testConstructor_Success() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            
            service = new PlantHealthService(repository);
            
            filesMock.verify(() -> Files.createDirectories(any(Path.class)));
        }
    }

    // --- TEST 2: Constructor Exception (Catch IO Exception) ---
    @Test
    void testConstructor_Exception() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Disk Full"));
            
            assertDoesNotThrow(() -> new PlantHealthService(repository));
        }
    }

    // --- TEST 3: Get Logs By User ---
    @Test
    void testGetLogsByUser() {
        // Init service safely without running actual IO
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        User user = new User();
        when(repository.findByUser(user)).thenReturn(Collections.emptyList());

        List<PlantHealth> result = service.getLogsByUser(user);
        assertNotNull(result);
        verify(repository).findByUser(user);
    }

    // --- TEST 4: Get Log By Id (Found & Not Found) ---
    @Test
    void testGetLogById() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        Long id = 1L;
        PlantHealth log = new PlantHealth();
        
        // Case Found
        when(repository.findById(id)).thenReturn(Optional.of(log));
        assertEquals(log, service.getLogById(id));

        // Case Not Found
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.getLogById(99L));
    }

    // --- TEST 5: Save Log - File is Null (Coverage Line 33: False condition 1) ---
    @Test
    void testSaveLog_FileIsNull() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        User user = new User();

        service.saveLog(log, null, user);

        assertEquals(user, log.getUser());
        verify(repository).save(log);
        assertNull(log.getImagePath());
    }

    // --- TEST 6: Save Log - File is Empty (Coverage Line 33: False condition 2) ---
    @Test
    void testSaveLog_FileIsEmpty() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        User user = new User();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        service.saveLog(log, file, user);

        verify(repository).save(log);
        verify(file, never()).getOriginalFilename();
    }

    // --- TEST 7: Save Log - New File, No Old Image (Coverage Line 37: False condition 1) ---
    @Test
    void testSaveLog_NewFile_NoOldImage() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath(null); // No old image
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("doc.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        // UUID disiapkan di luar try-catch mock untuk menghindari UnfinishedStubbing
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class);
             MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {
            
            uuidMock.when(UUID::randomUUID).thenReturn(fixedUuid);
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveLog(log, file, user);

            // Verify: Delete logic skipped (karena imagePath null)
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            assertTrue(log.getImagePath().contains("doc.jpg"));
            verify(repository).save(log);
        }
    }

    // --- TEST 8: Save Log - New File, Old Image is Empty String (Coverage Line 37: False condition 2) ---
    @Test
    void testSaveLog_NewFile_OldImageEmptyString() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath(""); // Empty String (bukan null)
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("doc.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class);
             MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {
            
            uuidMock.when(UUID::randomUUID).thenReturn(fixedUuid);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveLog(log, file, user);

            // Verify: Delete logic skipped (karena imagePath empty)
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
        }
    }

    // --- TEST 9: Save Log - New File, Old Image Exists (Coverage Line 37: True) ---
    @Test
    void testSaveLog_NewFile_ReplaceOldImage() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath("old.jpg"); // Has old image
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveLog(log, file, user);

            // Verify: Delete logic executed
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)));
            assertTrue(log.getImagePath().contains("new.jpg"));
        }
    }

    // --- TEST 10: Save Log - Delete Exception Ignored (Coverage Line 38 Catch) ---
    @Test
    void testSaveLog_DeleteException() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath("old.jpg");
        User user = new User();
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);
            
            // Force Exception on Delete
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Locked"));

            assertDoesNotThrow(() -> service.saveLog(log, file, user));
        }
    }

    // --- TEST 11: Delete Log - Not Found (Coverage Line 47: False) ---
    @Test
    void testDeleteLog_NotFound() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }
        
        when(repository.findById(1L)).thenReturn(Optional.empty());
        
        service.deleteLog(1L);
        
        verify(repository, never()).deleteById(any());
    }

    // --- TEST 12: Delete Log - No Image (Coverage Line 48: False) ---
    @Test
    void testDeleteLog_Success_NoImage() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath(null); // Kondisi log ada, tapi gambar null
        when(repository.findById(1L)).thenReturn(Optional.of(log));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            service.deleteLog(1L);
            
            // Pastikan tidak ada delete file
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            // Tapi data DB tetap dihapus
            verify(repository).deleteById(1L);
        }
    }

    // --- TEST 13: Delete Log - With Image (Coverage Line 48: True) ---
    @Test
    void testDeleteLog_Success_WithImage() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath("img.jpg");
        when(repository.findById(1L)).thenReturn(Optional.of(log));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            
            service.deleteLog(1L);
            
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)));
            verify(repository).deleteById(1L);
        }
    }

    // --- TEST 14: Delete Log - Exception Ignored (Coverage Line 49 Catch) ---
    @Test
    void testDeleteLog_Exception() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new PlantHealthService(repository);
        }

        PlantHealth log = new PlantHealth();
        log.setImagePath("img.jpg");
        when(repository.findById(1L)).thenReturn(Optional.of(log));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.deleteIfExists(any())).thenThrow(new IOException("Fail"));
            
            service.deleteLog(1L);
            
            verify(repository).deleteById(1L);
        }
    }
}