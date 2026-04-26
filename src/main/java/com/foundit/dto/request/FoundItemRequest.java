package com.foundit.dto.request;

import java.time.LocalDate;

public class FoundItemRequest {
    private String locationFound;
    private LocalDate dateFound;
    private String description;

    public String getLocationFound() { return this.locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }

    public LocalDate getDateFound() { return this.dateFound; }
    public void setDateFound(LocalDate dateFound) { this.dateFound = dateFound; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
}
