package org.delcom.app.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CareLogTest {

    private final UUID TEST_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID TEST_PLANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final String TEST_ACTIVITY = "Penyiraman";
    private final String TEST_NOTES = "Memberikan 500ml air hujan.";
    private final LocalDateTime TEST_DATE = LocalDateTime.of(2023, 11, 15, 10, 0);

    /**
     * Test untuk Constructor, Getter, dan Setter.
     * Mencakup: Constructor default, semua Setter, dan semua Getter.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        CareLog log = new CareLog();

        // 1. Test Constructor dan default state (semua harus null)
        assertNull(log.getId(), "ID seharusnya null secara default");
        assertNull(log.getPlantId(), "PlantId seharusnya null secara default");
        assertNull(log.getActivityType(), "ActivityType seharusnya null secara default");
        assertNull(log.getNotes(), "Notes seharusnya null secara default");
        assertNull(log.getLogDate(), "LogDate seharusnya null secara default");

        // 2. Set semua nilai
        log.setId(TEST_ID);
        log.setPlantId(TEST_PLANT_ID);
        log.setActivityType(TEST_ACTIVITY);
        log.setNotes(TEST_NOTES);
        log.setLogDate(TEST_DATE);

        // 3. Test semua Getter
        assertEquals(TEST_ID, log.getId());
        assertEquals(TEST_PLANT_ID, log.getPlantId());
        assertEquals(TEST_ACTIVITY, log.getActivityType());
        assertEquals(TEST_NOTES, log.getNotes());
        assertEquals(TEST_DATE, log.getLogDate());
    }
}