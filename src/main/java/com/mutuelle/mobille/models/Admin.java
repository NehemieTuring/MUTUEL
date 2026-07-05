package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;

    @Column(name = "is_active")
    private Boolean isActive=true;

    @Column(name = "pin", nullable = false, length = 4)
    private String pin = "2025";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt ;

    @Column(name = "updated_at", nullable = false )
    private LocalDateTime updatedAt ;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pin == null) {
            pin = "2025";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
