package org.delcom.app.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PlantHealthTest {

    private final Long TEST_ID = 1L;
    private final String TEST_PLANT_NAME = "My Monstera";
    private final String TEST_ISSUE = "Daun menguning di pinggir";
    private final String TEST_DIAGNOSIS = "Overwatering ringan";
    private final String TEST_STATUS = "Sedang ditangani";
    private final LocalDate TEST_DATE = LocalDate.of(2023, 11, 15);
    private final String TEST_IMAGE_PATH = "/images/log1.jpg";
    private final User TEST_USER = mock(User.class); // Mock User untuk relasi

    /**
     * Test untuk Constructor dan inisialisasi default.
     * Mencakup: Constructor default, dan memastikan 'date' disetel ke LocalDate.now().
     */
    @Test
    void constructor_ShouldInitializeDateToNow() {
        PlantHealth log = new PlantHealth();

        // Memastikan Date disetel ke hari ini (dengan margin error kecil)
        assertEquals(LocalDate.now(), log.getDate());
        
        // Memastikan properti lain default ke null
        assertNull(log.getId());
        assertNull(log.getPlantName());
        // ... (dan properti lainnya)
    }

    /**
     * Test untuk semua Getter dan Setter.
     * Mencakup: semua Setter dan semua Getter.
     */
    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        PlantHealth log = new PlantHealth();

        // 2. Set semua nilai
        log.setId(TEST_ID);
        log.setPlantName(TEST_PLANT_NAME);
        log.setIssue(TEST_ISSUE);
        log.setDiagnosis(TEST_DIAGNOSIS);
        log.setStatus(TEST_STATUS);
        log.setDate(TEST_DATE);
        log.setImagePath(TEST_IMAGE_PATH);
        log.setUser(TEST_USER);

        // 3. Test semua Getter
        assertEquals(TEST_ID, log.getId());
        assertEquals(TEST_PLANT_NAME, log.getPlantName());
        assertEquals(TEST_ISSUE, log.getIssue());
        assertEquals(TEST_DIAGNOSIS, log.getDiagnosis());
        assertEquals(TEST_STATUS, log.getStatus());
        assertEquals(TEST_DATE, log.getDate());
        assertEquals(TEST_IMAGE_PATH, log.getImagePath());
        assertEquals(TEST_USER, log.getUser());
    }
}