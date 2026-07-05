package com.mutuelle.mobille.dto.bilan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberExerciceBilanDTO {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private Long id;
    private Long memberId;
    private String memberFirstname;
    private String memberLastname;
    private Long exerciceId;
    private String exerciceName;
    private LocalDateTime exerciceStartDate;
    private LocalDateTime exerciceEndDate;
    private Integer sessionsCount;
    private LocalDateTime createdAt;

    // ── Versements cumulés sur l'exercice ─────────────────────────────────────
    private BigDecimal totalSolidaritePaid;
    private BigDecimal totalEpargneDeposited;
    private BigDecimal totalRegistrationPaid;
    private BigDecimal totalRenfoulementPaid;
    private BigDecimal totalRemboursementAmount;

    // ── Décaissements / Dettes cumulés ────────────────────────────────────────
    private BigDecimal totalEpargneWithdrawn;
    private BigDecimal totalEmpruntAmount;
    private BigDecimal totalInteretAmount;
    private BigDecimal totalAssistanceReceived;
    private BigDecimal totalAgapeShare;
    private BigDecimal renfoulementDistributed;

    // ── Calculés ──────────────────────────────────────────────────────────────
    private BigDecimal totalVerse;      // somme des versements
    private BigDecimal totalRecu;       // somme des décaissements
    private BigDecimal netExercice;     // totalVerse - totalRecu

    // ── Snapshot du compte à la clôture de l'exercice ─────────────────────────
    private BigDecimal snapshotSavingAmount;
    private BigDecimal snapshotBorrowAmount;
    private BigDecimal snapshotUnpaidSolidarity;
    private BigDecimal snapshotUnpaidRegistration;
    private BigDecimal snapshotUnpaidRenfoulement;
}
