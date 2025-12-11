// PASTIkan file ini ADA di folder: src/test/java/org/delcom/app/entities/

package org.delcom.app.entities;

import org.junit.jupiter.api.Test; // <-- Ini akan ter-resolve
// import static org.mockito.Mockito.mock; // <-- Ini akan ter-resolve

import static org.junit.jupiter.api.Assertions.*; // <-- Ini akan ter-resolve
import static org.mockito.Mockito.mock; // <-- Import ini sudah benar

class ScheduleTest {

    private final Long TEST_ID = 1L;
    private final String TEST_DATE = "2023-12-25";
    private final String TEST_ACTIVITY = "Pemupukan Natal";
    private final String TEST_PLANT_NAME = "Semua Anggrek";
    private final User TEST_USER = mock(User.class); // Mock User untuk relasi

    /**
     * Test untuk Constructor, Getter, dan Setter.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        Schedule schedule = new Schedule();

        // 1. Test Constructor dan default state (semua harus null)
        assertNull(schedule.getId(), "ID seharusnya null secara default");
        assertNull(schedule.getDate(), "Date seharusnya null secara default");
        assertNull(schedule.getActivity(), "Activity seharusnya null secara default");
        assertNull(schedule.getPlantName(), "PlantName seharusnya null secara default");
        assertNull(schedule.getUser(), "User seharusnya null secara default");

        // 2. Set semua nilai
        schedule.setId(TEST_ID);
        schedule.setDate(TEST_DATE);
        schedule.setActivity(TEST_ACTIVITY);
        schedule.setPlantName(TEST_PLANT_NAME);
        schedule.setUser(TEST_USER);

        // 3. Test semua Getter
        assertEquals(TEST_ID, schedule.getId());
        assertEquals(TEST_DATE, schedule.getDate());
        assertEquals(TEST_ACTIVITY, schedule.getActivity());
        assertEquals(TEST_PLANT_NAME, schedule.getPlantName());
        assertEquals(TEST_USER, schedule.getUser());
    }
}