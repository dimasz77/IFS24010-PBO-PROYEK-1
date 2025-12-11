package org.delcom.app.services;

import org.delcom.app.entities.Encyclopedia;
import org.delcom.app.repositories.EncyclopediaRepository;
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
class EncyclopediaServiceTest {

    @Mock
    private EncyclopediaRepository repository;

    private EncyclopediaService service;

    // --- TEST 1: Constructor Success (Create Directory) ---
    @Test
    void testConstructor_Success() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            
            service = new EncyclopediaService(repository);
            
            filesMock.verify(() -> Files.createDirectories(any(Path.class)));
        }
    }

    // --- TEST 2: Constructor Exception (Catch Block Line 26) ---
    @Test
    void testConstructor_Exception() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Permission Denied"));
            
            // Assert no exception thrown to caller (handled in constructor)
            assertDoesNotThrow(() -> new EncyclopediaService(repository));
        }
    }

    // --- TEST 3: Get All Entries ---
    @Test
    void testGetAllEntries() {
        // Mock static Files/Paths just to instantiate service safely
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<Encyclopedia> result = service.getAllEntries();
        assertNotNull(result);
        verify(repository).findAll();
    }

    // --- TEST 4: Get Entry By Id (Found & Not Found) ---
    @Test
    void testGetEntryById() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Long id = 1L;
        Encyclopedia entry = new Encyclopedia();
        
        // Case Found
        when(repository.findById(id)).thenReturn(Optional.of(entry));
        assertEquals(entry, service.getEntryById(id));

        // Case Not Found
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.getEntryById(99L));
    }

    // --- TEST 5: Save Entry - File is Null (Line 34: False) ---
    @Test
    void testSaveEntry_FileIsNull() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        service.saveEntry(entry, null);

        verify(repository).save(entry);
        assertNull(entry.getImagePath());
    }

    // --- TEST 6: Save Entry - File is Empty (Line 34: False) ---
    @Test
    void testSaveEntry_FileIsEmpty() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        service.saveEntry(entry, file);

        verify(repository).save(entry);
        verify(file, never()).getOriginalFilename();
    }

    // --- TEST 7: Save Entry - New File, No Old Image (Line 34: True, Line 40: False) ---
    @Test
    void testSaveEntry_NewFile_NoOldImage() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath(null); // No old image
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        // Prepare UUID outside stubbing
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class);
             MockedStatic<UUID> uuidMock = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {

            uuidMock.when(UUID::randomUUID).thenReturn(fixedUuid);
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveEntry(entry, file);

            // Verify delete skipped
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            assertTrue(entry.getImagePath().contains("new.jpg"));
            verify(repository).save(entry);
        }
    }

    // --- TEST 8: Save Entry - New File, Old Image is Empty String (Line 40: False) ---
    @Test
    void testSaveEntry_NewFile_OldImageEmpty() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath(""); // Empty string
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {

            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveEntry(entry, file);

            // Verify delete skipped because old image path was empty
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            assertTrue(entry.getImagePath().contains("new.jpg"));
        }
    }

    // --- TEST 9: Save Entry - Replace Old Image (Line 40: True) ---
    @Test
    void testSaveEntry_ReplaceOldImage() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath("old.png"); // Exists
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {

            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            service.saveEntry(entry, file);

            // Verify delete executed
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)));
            assertTrue(entry.getImagePath().contains("new.png"));
        }
    }

    // --- TEST 10: Save Entry - Delete Exception Ignored (Line 41 Catch) ---
    @Test
    void testSaveEntry_DeleteException() throws IOException {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath("old.png");
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("new.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {

            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            // Force Exception
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Locked"));
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class))).thenReturn(1L);

            assertDoesNotThrow(() -> service.saveEntry(entry, file));
        }
    }

    // --- TEST 11: Delete Entry - Not Found (Line 51: False) ---
    @Test
    void testDeleteEntry_NotFound() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        when(repository.findById(1L)).thenReturn(Optional.empty());
        service.deleteEntry(1L);
        verify(repository, never()).deleteById(any());
    }

    // --- TEST 12: Delete Entry - No Image (Line 52: False) ---
    @Test
    void testDeleteEntry_NoImage() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath(null);
        when(repository.findById(1L)).thenReturn(Optional.of(entry));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            service.deleteEntry(1L);
            
            filesMock.verify(() -> Files.deleteIfExists(any()), never());
            verify(repository).deleteById(1L);
        }
    }

    // --- TEST 13: Delete Entry - With Image (Line 52: True) ---
    @Test
    void testDeleteEntry_WithImage() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath("img.jpg");
        when(repository.findById(1L)).thenReturn(Optional.of(entry));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            
            service.deleteEntry(1L);
            
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)));
            verify(repository).deleteById(1L);
        }
    }

    // --- TEST 14: Delete Entry - Exception Ignored (Line 54 Catch) ---
    @Test
    void testDeleteEntry_Exception() {
        try (MockedStatic<Files> f = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> p = Mockito.mockStatic(Paths.class)) {
            service = new EncyclopediaService(repository);
        }

        Encyclopedia entry = new Encyclopedia();
        entry.setImagePath("img.jpg");
        when(repository.findById(1L)).thenReturn(Optional.of(entry));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = Mockito.mockStatic(Paths.class)) {
            
            pathsMock.when(() -> Paths.get(anyString())).thenReturn(mock(Path.class));
            filesMock.when(() -> Files.deleteIfExists(any())).thenThrow(new IOException("Fail"));
            
            service.deleteEntry(1L);
            
            verify(repository).deleteById(1L);
        }
    }
}