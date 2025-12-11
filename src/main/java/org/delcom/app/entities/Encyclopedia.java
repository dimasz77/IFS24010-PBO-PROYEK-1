package org.delcom.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "encyclopedias")
public class Encyclopedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String species;      // Nama Spesies (Misal: Monstera)
    
    @Column(length = 1000)       // Agar muat teks panjang
    private String description;  // Penjelasan singkat
    
    @Column(length = 1000)
    private String careTips;     // Tips perawatan khusus
    
    private String imagePath;    // Foto tanaman

    public Encyclopedia() {}

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCareTips() { return careTips; }
    public void setCareTips(String careTips) { this.careTips = careTips; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}