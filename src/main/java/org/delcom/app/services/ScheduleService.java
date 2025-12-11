package org.delcom.app.services;

import org.delcom.app.entities.Schedule;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.ScheduleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository repository;

    public ScheduleService(ScheduleRepository repository) {
        this.repository = repository;
    }

    public List<Schedule> getSchedulesByUser(User user) {
        return repository.findByUser(user);
    }

    public void saveSchedule(Schedule schedule, User user) {
        schedule.setUser(user);
        repository.save(schedule);
    }

    public Schedule getScheduleById(Long id) { return repository.findById(id).orElse(null); }
    public void deleteSchedule(Long id) { repository.deleteById(id); }
}