package org.delcom.app.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlantFormTest {

    private final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
    private final String TEST_TITLE = "Siram Pagi";
    private final String TEST_DESC = "Siram semua tanaman hias di teras.";
    private final String TEST_CONFIRM_TITLE = "confirm-sirampagi";

    /**
     * Test untuk Constructor, Getter, dan Setter.
     * Mencakup: Constructor default, semua Setter, dan semua Getter.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        PlantForm form = new PlantForm();
        
        // 1. Test Constructor dan default state
        assertNull(form.getId(), "ID seharusnya null secara default");
        assertNull(form.getTitle(), "Title seharusnya null secara default");
        assertNull(form.getDescription(), "Description seharusnya null secara default");
        assertFalse(form.getIsFinished(), "isFinished seharusnya false secara default");
        assertNull(form.getConfirmTitle(), "ConfirmTitle seharusnya null secara default");

        // 2. Set semua nilai
        form.setId(TEST_UUID);
        form.setTitle(TEST_TITLE);
        form.setDescription(TEST_DESC);
        form.setIsFinished(true); // Set to true untuk menguji setter boolean
        form.setConfirmTitle(TEST_CONFIRM_TITLE);

        // 3. Test semua Getter
        assertEquals(TEST_UUID, form.getId());
        assertEquals(TEST_TITLE, form.getTitle());
        assertEquals(TEST_DESC, form.getDescription());
        assertTrue(form.getIsFinished());
        assertEquals(TEST_CONFIRM_TITLE, form.getConfirmTitle());
    }

    /**
     * Test khusus untuk setIsFinished(false) untuk melengkapi coverage boolean.
     */
    @Test
    void setIsFinished_CanBeSetToFalse() {
        PlantForm form = new PlantForm();
        
        // Default sudah false, kita set ke true dulu
        form.setIsFinished(true);
        assertTrue(form.getIsFinished());

        // Set kembali ke false
        form.setIsFinished(false);
        assertFalse(form.getIsFinished());
    }
}