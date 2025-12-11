package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
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
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService service;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        service = new FileStorageService();
        // Set field protected secara manual karena tidak ada @Value di unit test
        service.uploadDir = "./test-uploads"; 
    }

    // --- TEST 1: Store File - Directory Tidak Ada (Buat Baru) ---
    // Coverage: Baris 21-23, 27-38
    @Test
    void testStoreFile_CreateDir_Success() throws IOException {
        UUID todoId = UUID.randomUUID();
        String originalName = "image.png";

        when(multipartFile.getOriginalFilename()).thenReturn(originalName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // Setup: Directory belum ada -> harus createDirectories
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            
            // Setup: Copy berhasil
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(1L);

            String result = service.storeFile(multipartFile, todoId);

            // Verifikasi format nama file: cover_UUID.ext
            assertNotNull(result);
            assertTrue(result.startsWith("cover_" + todoId.toString()));
            assertTrue(result.endsWith(".png"));

            // Verifikasi createDirectories dipanggil
            filesMock.verify(() -> Files.createDirectories(any(Path.class)));
        }
    }

    // --- TEST 2: Store File - Directory Sudah Ada ---
    // Coverage: Baris 22 (False), lewati 23
    @Test
    void testStoreFile_DirExists() throws IOException {
        UUID todoId = UUID.randomUUID();
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // Setup: Directory SUDAH ada
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            
            service.storeFile(multipartFile, todoId);

            // Verifikasi createDirectories TIDAK dipanggil
            filesMock.verify(() -> Files.createDirectories(any(Path.class)), never());
        }
    }

    // --- TEST 3: Store File - Tanpa Extension / Nama Aneh ---
    // Coverage: Baris 29 (False case) -> Baris 30 dilewati (fileExtension tetap kosong)
    @Test
    void testStoreFile_NoExtension() throws IOException {
        UUID todoId = UUID.randomUUID();
        // Case: Filename tidak null tapi tidak ada titik
        when(multipartFile.getOriginalFilename()).thenReturn("filenametitk"); 
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            String result = service.storeFile(multipartFile, todoId);

            // Result harusnya "cover_UUID" tanpa akhiran apa-apa
            assertTrue(result.endsWith(todoId.toString())); 
            assertFalse(result.contains("."));
        }
    }

    // --- TEST 4: Store File - Filename Null ---
    // Coverage: Baris 29 (Null checks)
    @Test
    void testStoreFile_NullFilename() throws IOException {
        UUID todoId = UUID.randomUUID();
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            
            String result = service.storeFile(multipartFile, todoId);
            
            // Extension tetap kosong string ""
            assertTrue(result.contains("cover_"));
        }
    }

    // --- TEST 5: Delete File - Success ---
    // Coverage: Baris 42-45
    @Test
    void testDeleteFile_Success() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            boolean result = service.deleteFile("file.jpg");

            assertTrue(result);
        }
    }

    // --- TEST 6: Delete File - Exception (IO Error) ---
    // Coverage: Baris 46-47 (Catch block)
    @Test
    void testDeleteFile_Exception() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // Paksa throw IOException
            filesMock.when(() -> Files.deleteIfExists(any(Path.class)))
                    .thenThrow(new IOException("File locked"));

            boolean result = service.deleteFile("file.jpg");

            assertFalse(result); // Harus return false saat catch
        }
    }

    // --- TEST 7: Load File ---
    // Coverage: Baris 51-52
    @Test
    void testLoadFile() {
        Path path = service.loadFile("test.jpg");
        assertNotNull(path);
        // Cek apakah path diakhiri dengan nama file yang benar
        assertTrue(path.toString().endsWith("test.jpg")); 
    }

    // --- TEST 8: File Exists ---
    // Coverage: Baris 55-56
    @Test
    void testFileExists() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            boolean exists = service.fileExists("ada.jpg");

            assertTrue(exists);
            filesMock.verify(() -> Files.exists(any(Path.class)));
        }
    }
}