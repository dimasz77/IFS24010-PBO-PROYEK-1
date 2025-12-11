package org.delcom.app.controllers;

import org.delcom.app.entities.Schedule;
import org.delcom.app.services.ScheduleService;
import org.delcom.app.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final UserService userService;

    public ScheduleController(ScheduleService scheduleService, UserService userService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
    }

    private org.delcom.app.entities.User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserByEmail(auth.getName());
    }

    @GetMapping
    public String listSchedules(Model model) {
        model.addAttribute("scheduleList", scheduleService.getSchedulesByUser(getCurrentUser()));
        return "pages/schedule";
    }

    @GetMapping("/new")
    public String newScheduleForm(Model model) {
        model.addAttribute("schedule", new Schedule());
        return "pages/schedule-form";
    }

    @GetMapping("/{id}/edit")
    public String editScheduleForm(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.getScheduleById(id);
        if (schedule == null) return "redirect:/schedule?error=Data tidak ditemukan";
        model.addAttribute("schedule", schedule);
        return "pages/schedule-form";
    }

    @PostMapping("/save")
    public String saveSchedule(@ModelAttribute Schedule schedule) {
        scheduleService.saveSchedule(schedule, getCurrentUser());
        return "redirect:/schedule?success=Jadwal berhasil disimpan";
    }

    @GetMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/schedule?success=Jadwal berhasil dihapus";
    }
}