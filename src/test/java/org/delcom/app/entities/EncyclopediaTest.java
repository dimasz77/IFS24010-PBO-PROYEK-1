package org.delcom.app.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncyclopediaTest {

    private final Long TEST_ID = 1L;
    private final String TEST_SPECIES = "Monstera Deliciosa";
    private final String TEST_DESC = "Tanaman hias tropis dengan daun lebar berlubang.";
    private final String TEST_TIPS = "Siram seminggu sekali, hindari sinar matahari langsung.";
    private final String TEST_IMAGE_PATH = "/images/monstera.jpg";

    /**
     * Test untuk Constructor, Getter, dan Setter.
     * Mencakup: Constructor default, semua Setter, dan semua Getter.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        Encyclopedia encyclopedia = new Encyclopedia();

        // 1. Test Constructor dan default state (semua harus null/default)
        assertNull(encyclopedia.getId(), "ID seharusnya null secara default");
        assertNull(encyclopedia.getSpecies(), "Species seharusnya null secara default");
        assertNull(encyclopedia.getDescription(), "Description seharusnya null secara default");
        assertNull(encyclopedia.getCareTips(), "CareTips seharusnya null secara default");
        assertNull(encyclopedia.getImagePath(), "ImagePath seharusnya null secara default");

        // 2. Set semua nilai
        encyclopedia.setId(TEST_ID);
        encyclopedia.setSpecies(TEST_SPECIES);
        encyclopedia.setDescription(TEST_DESC);
        encyclopedia.setCareTips(TEST_TIPS);
        encyclopedia.setImagePath(TEST_IMAGE_PATH);

        // 3. Test semua Getter
        assertEquals(TEST_ID, encyclopedia.getId());
        assertEquals(TEST_SPECIES, encyclopedia.getSpecies());
        assertEquals(TEST_DESC, encyclopedia.getDescription());
        assertEquals(TEST_TIPS, encyclopedia.getCareTips());
        assertEquals(TEST_IMAGE_PATH, encyclopedia.getImagePath());
    }
}