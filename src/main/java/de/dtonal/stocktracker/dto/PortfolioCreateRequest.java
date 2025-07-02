package de.dtonal.stocktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PortfolioCreateRequest {

    @NotBlank(message = "Portfolio name cannot be blank.")
    @Size(min = 3, max = 100, message = "Portfolio name must be between 3 and 100 characters.")
    private String name;

    @Size(max = 255, message = "Description cannot be longer than 255 characters.")
    private String description;

    public PortfolioCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        PortfolioCreateRequest that = (PortfolioCreateRequest) obj;

        if (!name.equals(that.name))
            return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}