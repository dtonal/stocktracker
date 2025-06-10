package de.dtonal.stocktracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public enum Role {
    USER,
    ADMIN
}
