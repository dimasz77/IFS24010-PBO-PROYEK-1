package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "plant_health_logs")
public class PlantHealth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plantName;
    private String issue;
    private String diagnosis;
    private String status;
    private LocalDate date;
    private String imagePath;

    // RELASI KE USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PlantHealth() { this.date = LocalDate.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}