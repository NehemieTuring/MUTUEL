package com.mutuelle.mobille.models.bilan;

import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.Session;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_session_bilans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "session_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSessionBilan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    // ── Versements du membre (argent entré dans la mutuelle) ──────────────────

    @Builder.Default
    @Column(name = "solidarite_paid", precision = 12, scale = 2)
    private BigDecimal solidaritePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "epargne_deposited", precision = 12, scale = 2)
    private BigDecimal epargneDeposited = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "registration_paid", precision = 12, scale = 2)
    private BigDecimal registrationPaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "renfoulement_paid", precision = 12, scale = 2)
    private BigDecimal renfoulementPaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "remboursement_amount", precision = 12, scale = 2)
    private BigDecimal remboursementAmount = BigDecimal.ZERO;

    // ── Décaissements / Dettes du membre (argent sorti de la mutuelle) ────────

    @Builder.Default
    @Column(name = "epargne_withdrawn", precision = 12, scale = 2)
    private BigDecimal epargneWithdrawn = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "emprunt_amount", precision = 12, scale = 2)
    private BigDecimal empruntAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "interet_amount", precision = 12, scale = 2)
    private BigDecimal interetAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "assistance_received", precision = 12, scale = 2)
    private BigDecimal assistanceReceived = BigDecimal.ZERO;

    // Part agape de la session (session.agapeAmountPerMember)
    @Builder.Default
    @Column(name = "agape_share", precision = 12, scale = 2)
    private BigDecimal agapeShare = BigDecimal.ZERO;

    // ── Snapshot du compte à la clôture de la session ─────────────────────────

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
