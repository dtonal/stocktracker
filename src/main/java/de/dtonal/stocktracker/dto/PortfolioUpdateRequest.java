package de.dtonal.stocktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PortfolioUpdateRequest {
    @NotBlank(message = "Portfolio name cannot be blank.")
    @Size(min = 3, max = 100, message = "Portfolio name must be between 3 and 100 characters.")
    private String name;

    @Size(max = 255, message = "Description cannot be longer than 255 characters.")
    private String description;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 