    package org.delcom.app.services;

    import org.delcom.app.entities.Plant;
    import org.delcom.app.entities.User;
    import org.delcom.app.repositories.PlantRepository;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;
    import java.io.IOException;
    import java.nio.file.*;
    import java.time.LocalDate;
    import java.util.List;
    import java.util.UUID;

    @Service
    public class PlantService {
        private final PlantRepository plantRepository;
        private final String UPLOAD_DIR = "./uploads/";

        public PlantService(PlantRepository plantRepository) {
            this.plantRepository = plantRepository;
            try { Files.createDirectories(Paths.get(UPLOAD_DIR)); } 
            catch (IOException e) { e.printStackTrace(); }
        }

        // LIST BY USER
        public List<Plant> getPlantsByUser(User user) {
            return plantRepository.findByUser(user);
        }

        public Plant getPlantById(UUID id) { return plantRepository.findById(id).orElse(null); }

        // SAVE WITH USER
        public void savePlant(Plant plant, MultipartFile file, User user) throws IOException {
            plant.setUser(user); // Set Pemilik

            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                if (plant.getImagePath() != null && !plant.getImagePath().isEmpty() && !plant.getImagePath().equals(fileName)) {
                    try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + plant.getImagePath())); } catch (Exception ignored) {}
                }
                plant.setImagePath(fileName);
            }
            if (plant.getLastWatered() == null) plant.setLastWatered(LocalDate.now());
            plantRepository.save(plant);
        }

        public void deletePlant(UUID id) {
            Plant plant = getPlantById(id);
            if (plant != null) {
                if (plant.getImagePath() != null) {
                    try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + plant.getImagePath())); } catch (Exception ignored) {}
                }
                plantRepository.deleteById(id);
            }
        }

        public void waterPlantNow(UUID id) {
            Plant plant = getPlantById(id);
            if (plant != null) {
                plant.setLastWatered(LocalDate.now());
                plantRepository.save(plant);
            }
        }
    }