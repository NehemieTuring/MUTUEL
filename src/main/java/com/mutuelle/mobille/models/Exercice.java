package com.mutuelle.mobille.models;

import com.mutuelle.mobille.enums.StatusExercice;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import  com.mutuelle.mobille.enums.StatusExercice;

@Entity
@Table(name = "exercices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatusExercice status = StatusExercice.PLANNED;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToOne(mappedBy = "exercice", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExerciceHistory history;

    @OneToOne(mappedBy = "exercice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Renfoulement renfoulement;

    // Relation inverse : un exercice a plusieurs sessions
    @OneToMany(mappedBy = "exercice", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

}