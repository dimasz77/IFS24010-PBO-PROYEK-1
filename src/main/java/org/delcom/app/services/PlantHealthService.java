package org.delcom.app.services;

import org.delcom.app.entities.PlantHealth;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.PlantHealthRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class PlantHealthService {
    private final PlantHealthRepository repository;
    private final String UPLOAD_DIR = "./uploads/";

    public PlantHealthService(PlantHealthRepository repository) {
        this.repository = repository;
        try { Files.createDirectories(Paths.get(UPLOAD_DIR)); } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<PlantHealth> getLogsByUser(User user) {
        return repository.findByUser(user);
    }

    public PlantHealth getLogById(Long id) { return repository.findById(id).orElse(null); }

    public void saveLog(PlantHealth log, MultipartFile file, User user) throws IOException {
        log.setUser(user); // Set Pemilik

        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            if (log.getImagePath() != null && !log.getImagePath().isEmpty()) {
                try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + log.getImagePath())); } catch (Exception ignored) {}
            }
            log.setImagePath(fileName);
        }
        repository.save(log);
    }

    public void deleteLog(Long id) {
        PlantHealth log = getLogById(id);
        if (log != null) {
            if (log.getImagePath() != null) {
                try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + log.getImagePath())); } catch (Exception ignored) {}
            }
            repository.deleteById(id);
        }
    }
}