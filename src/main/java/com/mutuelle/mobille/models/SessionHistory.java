package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    // ── Assistances ───────────────────────────────────────────────────────────

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal totalAssistanceAmount = BigDecimal.ZERO;

    @Builder.Default
    private Long totalAssistanceCount = 0L;

    // ── Agapes ────────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal agapeAmount = BigDecimal.ZERO;

    // ── Solidarité collectée ──────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "total_solidarity_collected", precision = 12, scale = 2)
    private BigDecimal totalSolidarityCollected = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_solidarity_count")
    private Long totalSolidarityCount = 0L;

    // ── Épargne ───────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "total_epargne_deposited", precision = 12, scale = 2)
    private BigDecimal totalEpargneDeposited = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_epargne_withdrawn", precision = 12, scale = 2)
    private BigDecimal totalEpargneWithdrawn = BigDecimal.ZERO;

    // ── Emprunts & remboursements ─────────────────────────────────────────────

    @Builder.Default
    @Column(name = "total_emprunt_amount", precision = 12, scale = 2)
    private BigDecimal totalEmpruntAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_remboursement_amount", precision = 12, scale = 2)
    private BigDecimal totalRemboursementAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_interet_amount", precision = 12, scale = 2)
    private BigDecimal totalInteretAmount = BigDecimal.ZERO;

    // ── Renfoulement & inscription ────────────────────────────────────────────

    @Builder.Default
    @Column(name = "total_renfoulement_collected", precision = 12, scale = 2)
    private BigDecimal totalRenfoulementCollected = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_registration_collected", precision = 12, scale = 2)
    private BigDecimal totalRegistrationCollected = BigDecimal.ZERO;

    // ── Snapshot trésorerie mutuelle à la clôture ─────────────────────────────

    // Total trésorerie = épargne + solidarité + caisse inscription
    @Builder.Default
    @Column(name = "mutuelle_cash", precision = 12, scale = 2)
    private BigDecimal mutuelleCash = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "mutuelle_saving_amount", precision = 12, scale = 2)
    private BigDecimal mutuellesSavingAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "mutuelle_solidarity_amount", precision = 12, scale = 2)
    private BigDecimal mutuelleSolidarityAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "mutuelle_registration_amount", precision = 12, scale = 2)
    private BigDecimal mutuelleRegistrationAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "mutuelle_borrow_amount", precision = 12, scale = 2)
    private BigDecimal mutuelleBorrowAmount = BigDecimal.ZERO;

    // ── Compteurs ─────────────────────────────────────────────────────────────

    @Builder.Default
    private Long totalTransactions = 0L;

    @Builder.Default
    @Column(name = "active_members_count")
    private Long activeMembersCount = 0L;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(updatable = false)
    private LocalDateTime createdAt;

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
