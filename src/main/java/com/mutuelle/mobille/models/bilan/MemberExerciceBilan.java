package com.mutuelle.mobille.models.bilan;

import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_exercice_bilans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "exercice_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExerciceBilan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    // ── Versements cumulés sur l'exercice ─────────────────────────────────────

    @Builder.Default
    @Column(name = "total_solidarite_paid", precision = 12, scale = 2)
    private BigDecimal totalSolidaritePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_epargne_deposited", precision = 12, scale = 2)
    private BigDecimal totalEpargneDeposited = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_registration_paid", precision = 12, scale = 2)
    private BigDecimal totalRegistrationPaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_renfoulement_paid", precision = 12, scale = 2)
    private BigDecimal totalRenfoulementPaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_remboursement_amount", precision = 12, scale = 2)
    private BigDecimal totalRemboursementAmount = BigDecimal.ZERO;

    // ── Décaissements / Dettes cumulés sur l'exercice ─────────────────────────

    @Builder.Default
    @Column(name = "total_epargne_withdrawn", precision = 12, scale = 2)
    private BigDecimal totalEpargneWithdrawn = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_emprunt_amount", precision = 12, scale = 2)
    private BigDecimal totalEmpruntAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_interet_amount", precision = 12, scale = 2)
    private BigDecimal totalInteretAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_assistance_received", precision = 12, scale = 2)
    private BigDecimal totalAssistanceReceived = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_agape_share", precision = 12, scale = 2)
    private BigDecimal totalAgapeShare = BigDecimal.ZERO;

    // Renfoulement distribué à ce membre à la clôture de l'exercice
    @Builder.Default
    @Column(name = "renfoulement_distributed", precision = 12, scale = 2)
    private BigDecimal renfoulementDistributed = BigDecimal.ZERO;

    // Nombre de sessions de cet exercice
    @Builder.Default
    @Column(name = "sessions_count")
    private Integer sessionsCount = 0;

    // ── Snapshot du compte à la clôture de l'exercice ─────────────────────────

    @Builder.Default
    @Column(name = "snapshot_saving_amount", precision = 12, scale = 2)
    private BigDecimal snapshotSavingAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "snapshot_borrow_amount", precision = 12, scale = 2)
    private BigDecimal snapshotBorrowAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "snapshot_unpaid_solidarity", precision = 12, scale = 2)
    private BigDecimal snapshotUnpaidSolidarity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "snapshot_unpaid_registration", precision = 12, scale = 2)
    private BigDecimal snapshotUnpaidRegistration = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "snapshot_unpaid_renfoulement", precision = 12, scale = 2)
    private BigDecimal snapshotUnpaidRenfoulement = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
