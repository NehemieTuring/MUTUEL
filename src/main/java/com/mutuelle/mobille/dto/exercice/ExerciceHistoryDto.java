package com.mutuelle.mobille.dto.exercice;

import com.mutuelle.mobille.enums.StatusExercice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExerciceHistoryDto {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private Long id;
    private Long exerciceId;
    private String exerciceName;
    private LocalDateTime exerciceStartDate;
    private LocalDateTime exerciceEndDate;
    private StatusExercice exerciceStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Assistances ───────────────────────────────────────────────────────────
    private BigDecimal totalAssistanceAmount;
    private Long totalAssistanceCount;

    // ── Agapes ────────────────────────────────────────────────────────────────
    private BigDecimal totalAgapeAmount;

    // ── Solidarité collectée ──────────────────────────────────────────────────
    private BigDecimal totalSolidarityCollected;

    // ── Épargne ───────────────────────────────────────────────────────────────
    private BigDecimal totalEpargneDeposited;
    private BigDecimal totalEpargneWithdrawn;

    // ── Emprunts & remboursements ─────────────────────────────────────────────
    private BigDecimal totalEmpruntAmount;
    private BigDecimal totalRemboursementAmount;
    private BigDecimal totalInteretAmount;

    // ── Renfoulement ──────────────────────────────────────────────────────────
    private BigDecimal totalRenfoulementDistributed;
    private BigDecimal renfoulementUnitAmount;
    private BigDecimal totalRenfoulementCollected;

    // ── Inscription ───────────────────────────────────────────────────────────
    private BigDecimal totalRegistrationCollected;

    // ── Snapshot trésorerie mutuelle ──────────────────────────────────────────
    private BigDecimal mutuelleCash;
    private BigDecimal mutuellesSavingAmount;
    private BigDecimal mutuelleSolidarityAmount;
    private BigDecimal mutuelleRegistrationAmount;
    private BigDecimal mutuelleBorrowAmount;

    // ── Compteurs ─────────────────────────────────────────────────────────────
    private Long totalTransactions;
    private Integer sessionsCount;
    private Long activeMembersCount;
}
