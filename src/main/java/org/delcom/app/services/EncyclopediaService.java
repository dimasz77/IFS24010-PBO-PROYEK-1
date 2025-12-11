package org.delcom.app.services;

import org.delcom.app.entities.Encyclopedia;
import org.delcom.app.repositories.EncyclopediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class EncyclopediaService {

    private final EncyclopediaRepository repository;
    private final String UPLOAD_DIR = "./uploads/";

    public EncyclopediaService(EncyclopediaRepository repository) {
        this.repository = repository;
        createUploadDir();
    }

    private void createUploadDir() {
        try { Files.createDirectories(Paths.get(UPLOAD_DIR)); } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<Encyclopedia> getAllEntries() { return repository.findAll(); }

    public Encyclopedia getEntryById(Long id) { return repository.findById(id).orElse(null); }

    public void saveEntry(Encyclopedia entry, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Hapus gambar lama saat edit
            if (entry.getImagePath() != null && !entry.getImagePath().isEmpty()) {
                try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + entry.getImagePath())); } 
                catch (Exception ignored) {}
            }
            entry.setImagePath(fileName);
        }
        repository.save(entry);
    }

    public void deleteEntry(Long id) {
        Encyclopedia entry = getEntryById(id);
        if (entry != null) {
            if (entry.getImagePath() != null) {
                try { Files.deleteIfExists(Paths.get(UPLOAD_DIR + entry.getImagePath())); } 
                catch (Exception ignored) {}
            }
            repository.deleteById(id);
        }
    }
}