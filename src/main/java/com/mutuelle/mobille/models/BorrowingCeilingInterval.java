package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_ceiling_intervals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingCeilingInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_epargne", nullable = false)
    private BigDecimal minEpargne;

    @Column(name = "max_epargne")
    private BigDecimal maxEpargne; // Null indicates no upper limit

    @Column(name = "multiplier", nullable = false)
    private BigDecimal multiplier;

    @Column(name = "max_plafond")
    private BigDecimal maxPlafond; // Null indicates no cap

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}