package org.delcom.app.controllers;

import org.delcom.app.entities.PlantHealth;
import org.delcom.app.services.PlantHealthService;
import org.delcom.app.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final PlantHealthService service;
    private final UserService userService;

    public DoctorController(PlantHealthService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    private org.delcom.app.entities.User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByEmail(auth.getName());
    }

    @GetMapping
    public String listDoctor(Model model) {
        model.addAttribute("logs", service.getLogsByUser(getCurrentUser()));
        return "pages/doctor"; 
    }

    @GetMapping("/new")
    public String newLog(Model model) {
        model.addAttribute("plantHealth", new PlantHealth());
        return "pages/doctor-form";
    }

    @GetMapping("/{id}/edit")
    public String editLog(@PathVariable Long id, Model model) {
        PlantHealth log = service.getLogById(id);
        if (log == null) return "redirect:/doctor?error=Data tidak ditemukan";
        model.addAttribute("plantHealth", log);
        return "pages/doctor-form";
    }

    @PostMapping("/save")
    public String saveLog(@ModelAttribute PlantHealth plantHealth, @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            service.saveLog(plantHealth, imageFile, getCurrentUser());
        } catch (Exception e) {
            return "redirect:/doctor?error=Gagal menyimpan data";
        }
        return "redirect:/doctor?success=Catatan berhasil disimpan";
    }

    @GetMapping("/{id}/delete")
    public String deleteLog(@PathVariable Long id) {
        service.deleteLog(id);
        return "redirect:/doctor?success=Catatan berhasil dihapus";
    }
}