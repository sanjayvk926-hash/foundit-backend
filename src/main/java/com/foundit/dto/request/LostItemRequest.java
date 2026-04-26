package com.foundit.dto.request;

import java.time.LocalDate;

public class LostItemRequest {
    private String title;
    private String description;
    private String category;
    private String color;
    private LocalDate dateLost;
    private String locationLost;

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return this.category; }
    public void setCategory(String category) { this.category = category; }

    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }

    public LocalDate getDateLost() { return this.dateLost; }
    public void setDateLost(LocalDate dateLost) { this.dateLost = dateLost; }

    public String getLocationLost() { return this.locationLost; }
    public void setLocationLost(String locationLost) { this.locationLost = locationLost; }
}
