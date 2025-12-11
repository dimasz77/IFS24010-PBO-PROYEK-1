package org.delcom.app.controllers;

import org.delcom.app.entities.Plant;
import org.delcom.app.entities.User;
import org.delcom.app.services.PlantService;
import org.delcom.app.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/plants")
public class PlantController {
    private final PlantService plantService;
    private final UserService userService;

    public PlantController(PlantService plantService, UserService userService) {
        this.plantService = plantService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByEmail(auth.getName());
    }

    @GetMapping
    public String listPlants(Model model) {
        try { 
            User user = getCurrentUser();
            List<Plant> plants = plantService.getPlantsByUser(user); // Data Spesifik User
            model.addAttribute("plants", plants);
            
            // Statistik
            int totalPlants = plants.size();
            LocalDate today = LocalDate.now();
            long needsWater = plants.stream().filter(p -> {
                if (p.getLastWatered() == null || p.getWateringFrequency() == null) return false;
                LocalDate nextWatering = p.getLastWatered().plusDays(p.getWateringFrequency());
                return !nextWatering.isAfter(today);
            }).count();
            long healthy = totalPlants - needsWater;
            long uniqueSpecies = plants.stream().map(p -> p.getSpecies() != null ? p.getSpecies().toLowerCase() : "lainnya").distinct().count();

            model.addAttribute("totalPlants", totalPlants);
            model.addAttribute("needsWater", needsWater);
            model.addAttribute("healthy", healthy);
            model.addAttribute("uniqueSpecies", uniqueSpecies);
        } catch (Exception e) { 
            model.addAttribute("plants", Collections.emptyList());
        }
        return "pages/plants/list"; 
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("plant", new Plant());
        return "pages/plants/form"; 
    }

    @PostMapping("/save")
    public String savePlant(@ModelAttribute Plant plant, @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            plantService.savePlant(plant, imageFile, getCurrentUser());
        } catch (Exception e) {
            return "redirect:/plants/new?error=Gagal menyimpan data";
        }
        return "redirect:/plants?success=Data tanaman berhasil disimpan"; 
    }

    @GetMapping("/{id}")
    public String detailPlant(@PathVariable UUID id, Model model) {
        Plant plant = plantService.getPlantById(id);
        if (plant == null) return "redirect:/plants?error=Data tidak ditemukan";
        model.addAttribute("plant", plant);
        return "pages/plants/detail"; 
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Plant plant = plantService.getPlantById(id);
        if (plant == null) return "redirect:/plants?error=Data tidak ditemukan";
        model.addAttribute("plant", plant);
        return "pages/plants/form"; 
    }

    @GetMapping("/{id}/delete")
    public String deletePlant(@PathVariable UUID id) {
        plantService.deletePlant(id);
        return "redirect:/plants?success=Tanaman berhasil dihapus";
    }

    @GetMapping("/{id}/water")
    public String waterPlantNow(@PathVariable UUID id) {
        plantService.waterPlantNow(id);
        return "redirect:/plants?success=Segar! Tanaman berhasil disiram.";
    }
}