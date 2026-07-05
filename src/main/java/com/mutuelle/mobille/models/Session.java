package com.mutuelle.mobille.models;

import com.mutuelle.mobille.enums.StatusExercice;
import com.mutuelle.mobille.enums.StatusSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /*@Column(name = "solidarity_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal solidarityAmount = BigDecimal.ZERO;*/

    @Column(name = "agape_amount_per_member", precision = 12, scale = 2, nullable = false)
    private BigDecimal agapeAmountPerMember = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDateTime startDate ;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private StatusSession status = StatusSession.PLANNED;

    // Une session appartient à un exercice
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    // Relation inverse : une session a plusieurs assistances
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assistance> assistances = new HashSet<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private SessionHistory history;

    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant ne peut pas être négatif")
    @Column(name = "total_interet_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalInteretAmount = BigDecimal.ZERO;

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