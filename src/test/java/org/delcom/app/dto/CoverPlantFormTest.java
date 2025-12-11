package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoverTodoFormTest {

    // UUID unik untuk pengujian
    private final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");

    /**
     * Test untuk Constructor dan Getter/Setter.
     * Mencakup: Constructor default, setId, getId, setCoverFile, getCoverFile.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        CoverPlantForm form = new CoverPlantForm();
        
        // 1. Test Constructor dan default state
        assertNull(form.getId());
        assertNull(form.getCoverFile());

        // 2. Test Setters/Getters
        form.setId(TEST_UUID);
        MockMultipartFile mockFile = new MockMultipartFile("data", "filename.png", "image/png", "some data".getBytes());
        form.setCoverFile(mockFile);

        assertEquals(TEST_UUID, form.getId());
        assertEquals(mockFile, form.getCoverFile());
    }

    // --- Test Helper Methods ---

    /**
     * Test untuk isEmpty() - Skenario Cover NULL.
     * Mencakup: if (coverFile == null) return true;
     */
    @Test
    void isEmpty_WhenFileIsNull_ShouldReturnTrue() {
        CoverPlantForm form = new CoverPlantForm(); // coverFile = null
        assertTrue(form.isEmpty());
    }

    /**
     * Test untuk isEmpty() - Skenario Cover Kosong.
     * Mencakup: if (coverFile.isEmpty()) return true;
     */
    @Test
    void isEmpty_WhenFileIsEmpty_ShouldReturnTrue() {
        CoverPlantForm form = new CoverPlantForm();
        // Mock MultipartFile yang isEmpty() mengembalikan true
        MockMultipartFile emptyFile = new MockMultipartFile("data", "", "text/plain", new byte[0]); 
        form.setCoverFile(emptyFile);
        
        assertTrue(form.isEmpty());
    }
    
    /**
     * Test untuk isEmpty() - Skenario Cover ADA.
     * Mencakup: return false.
     */
    @Test
    void isEmpty_WhenFileIsPresent_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm();
        MockMultipartFile presentFile = new MockMultipartFile("data", "file.jpg", "image/jpeg", "data".getBytes());
        form.setCoverFile(presentFile);
        
        assertFalse(form.isEmpty());
    }
    
    /**
     * Test untuk getOriginalFilename() - Skenario File ADA.
     * Mencakup: coverFile != null ? ... : null; (True)
     */
    @Test
    void getOriginalFilename_WhenFileIsPresent_ShouldReturnFilename() {
        CoverPlantForm form = new CoverPlantForm();
        MockMultipartFile file = new MockMultipartFile("data", "test-cover.jpg", "image/jpeg", "data".getBytes());
        form.setCoverFile(file);

        assertEquals("test-cover.jpg", form.getOriginalFilename());
    }

    /**
     * Test untuk getOriginalFilename() - Skenario File NULL.
     * Mencakup: coverFile != null ? ... : null; (False)
     */
    @Test
    void getOriginalFilename_WhenFileIsNull_ShouldReturnNull() {
        CoverPlantForm form = new CoverPlantForm(); // coverFile = null
        assertNull(form.getOriginalFilename());
    }


    // --- Test Validation Methods ---
    
    /**
     * Test untuk isValidImage() - Skenario File NULL/Kosong.
     * Mencakup: if (this.isEmpty()) { return false; }
     */
    @Test
    void isValidImage_WhenFileIsEmpty_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm(); // file is null -> isEmpty() is true
        assertFalse(form.isValidImage());
        
        MockMultipartFile emptyFile = new MockMultipartFile("data", "", "text/plain", new byte[0]);
        form.setCoverFile(emptyFile);
        assertFalse(form.isValidImage());
    }

    /**
     * Test untuk isValidImage() - Skenario ContentType NULL.
     * Mencakup: contentType != null && ... (False)
     */
    @Test
    void isValidImage_WhenContentTypeIsNull_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm();
        // Menggunakan Mockito untuk memalsukan MultipartFile yang mengembalikan null untuk ContentType
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn(null); // Skenario ContentType null
        form.setCoverFile(mockFile);

        assertFalse(form.isValidImage());
    }

    /**
     * Test untuk isValidImage() - Skenario Tipe file Valid (JPEG, PNG, GIF, WEBP).
     * Mencakup: semua cabang (image/jpeg, image/png, image/gif, image/webp)
     */
    @Test
    void isValidImage_WhenContentTypeIsAllowed_ShouldReturnTrue() {
        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        
        for (String type : allowedTypes) {
            CoverPlantForm form = new CoverPlantForm();
            MockMultipartFile file = new MockMultipartFile("data", "file." + type.substring(6), type, "data".getBytes());
            form.setCoverFile(file);
            
            assertTrue(form.isValidImage(), "Seharusnya valid untuk tipe: " + type);
        }
    }

    /**
     * Test untuk isValidImage() - Skenario Tipe file TIDAK Valid.
     * Mencakup: contentType.equals("...") || ... (False)
     */
    @Test
    void isValidImage_WhenContentTypeIsNotAllowed_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm();
        MockMultipartFile file = new MockMultipartFile("data", "file.pdf", "application/pdf", "data".getBytes());
        form.setCoverFile(file);

        assertFalse(form.isValidImage());
    }


    /**
     * Test untuk isSizeValid() - Skenario File NULL.
     * Mencakup: coverFile != null && ... (False)
     */
    @Test
    void isSizeValid_WhenFileIsNull_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm(); // coverFile = null
        assertFalse(form.isSizeValid(1024L));
    }

    /**
     * Test untuk isSizeValid() - Skenario Ukuran Valid (Kurang dari maxSize).
     * Mencakup: coverFile.getSize() <= maxSize (True)
     */
    @Test
    void isSizeValid_WhenSizeIsBelowMax_ShouldReturnTrue() {
        CoverPlantForm form = new CoverPlantForm();
        MockMultipartFile file = new MockMultipartFile("data", "file.jpg", "image/jpeg", new byte[500]);
        form.setCoverFile(file);

        assertTrue(form.isSizeValid(1024L)); // 500 <= 1024
    }

    /**
     * Test untuk isSizeValid() - Skenario Ukuran Tidak Valid (Lebih dari maxSize).
     * Mencakup: coverFile.getSize() <= maxSize (False)
     */
    @Test
    void isSizeValid_WhenSizeIsAboveMax_ShouldReturnFalse() {
        CoverPlantForm form = new CoverPlantForm();
        MockMultipartFile file = new MockMultipartFile("data", "file.jpg", "image/jpeg", new byte[1500]);
        form.setCoverFile(file);

        assertFalse(form.isSizeValid(1024L)); // 1500 > 1024
    }
}
