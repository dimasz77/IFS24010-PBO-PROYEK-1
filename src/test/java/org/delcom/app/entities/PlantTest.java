package org.delcom.app.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PlantTest {

    private final UUID TEST_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final String TEST_NAME = "My Green Cactus";
    private final String TEST_SPECIES = "Cactaceae";
    private final String TEST_DESC = "Cactus favorit saya, hadiah dari ibu.";
    private final String TEST_IMAGE_PATH = "/images/cactus.jpg";
    private final Integer TEST_FREQ = 7;
    private final LocalDate TEST_DATE = LocalDate.of(2023, 11, 15);
    private final User TEST_USER = mock(User.class); // Mock User untuk relasi

    /**
     * Test untuk Constructor, Getter, dan Setter.
     * Mencakup: Constructor default, semua Setter, dan semua Getter.
     */
    @Test
    void constructorAndGettersSetters_ShouldWorkCorrectly() {
        Plant plant = new Plant();

        // 1. Test Constructor dan default state (semua harus null/default)
        assertNull(plant.getId(), "ID seharusnya null secara default");
        assertNull(plant.getName(), "Name seharusnya null secara default");
        assertNull(plant.getSpecies(), "Species seharusnya null secara default");
        assertNull(plant.getDescription(), "Description seharusnya null secara default");
        assertNull(plant.getImagePath(), "ImagePath seharusnya null secara default");
        assertNull(plant.getWateringFrequency(), "WateringFrequency seharusnya null secara default");
        assertNull(plant.getLastWatered(), "LastWatered seharusnya null secara default");
        assertNull(plant.getUser(), "User seharusnya null secara default");

        // 2. Set semua nilai
        plant.setId(TEST_ID);
        plant.setName(TEST_NAME);
        plant.setSpecies(TEST_SPECIES);
        plant.setDescription(TEST_DESC);
        plant.setImagePath(TEST_IMAGE_PATH);
        plant.setWateringFrequency(TEST_FREQ);
        plant.setLastWatered(TEST_DATE);
        plant.setUser(TEST_USER);

        // 3. Test semua Getter
        assertEquals(TEST_ID, plant.getId());
        assertEquals(TEST_NAME, plant.getName());
        assertEquals(TEST_SPECIES, plant.getSpecies());
        assertEquals(TEST_DESC, plant.getDescription());
        assertEquals(TEST_IMAGE_PATH, plant.getImagePath());
        assertEquals(TEST_FREQ, plant.getWateringFrequency());
        assertEquals(TEST_DATE, plant.getLastWatered());
        assertEquals(TEST_USER, plant.getUser());
    }
}
