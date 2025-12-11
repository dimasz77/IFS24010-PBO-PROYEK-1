package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "plants")
public class Plant {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;
    private String species;
    private String description;
    private String imagePath; 
    private Integer wateringFrequency; 
    private LocalDate lastWatered;

    // RELASI KE USER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Plant() {}

    // Getters Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Integer getWateringFrequency() { return wateringFrequency; }
    public void setWateringFrequency(Integer wateringFrequency) { this.wateringFrequency = wateringFrequency; }
    public LocalDate getLastWatered() { return lastWatered; }
    public void setLastWatered(LocalDate lastWatered) { this.lastWatered = lastWatered; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}