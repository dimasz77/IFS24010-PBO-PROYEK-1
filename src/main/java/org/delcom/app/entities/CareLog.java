package org.delcom.app.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class CareLog {
    private UUID id;
    private UUID plantId;
    private String activityType; 
    private String notes;        
    private LocalDateTime logDate;

    public CareLog() {}

    // --- GETTERS AND SETTERS ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPlantId() { return plantId; }
    public void setPlantId(UUID plantId) { this.plantId = plantId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }
}