package de.dtonal.stocktracker.dto;

import java.time.LocalDateTime;
import java.util.Set;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;

// Repräsentiert die Daten eines Benutzers, die an den Client gesendet werden
public class UserResponse {
    private final long id;
    private final String name;
    private final String email;
    private final Set<Role> roles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
