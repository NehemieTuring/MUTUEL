package com.mutuelle.mobille.dto.sessionHistory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionHistoryResponseDTO {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private Long id;
    private Long sessionId;
    private String sessionName;
    private String exerciceName;
    private LocalDateTime sessionStartDate;
    private LocalDateTime sessionEndDate;
    private LocalDateTime createdAt;

    // ── Assistances ───────────────────────────────────────────────────────────
    private BigDecimal totalAssistanceAmount;
    private Long totalAssistanceCount;

    // ── Agapes ────────────────────────────────────────────────────────────────
    private BigDecimal agapeAmount;

    // ── Solidarité collectée ──────────────────────────────────────────────────
    private BigDecimal totalSolidarityCollected;
    private Long totalSolidarityCount;

    // ── Épargne ───────────────────────────────────────────────────────────────
    private BigDecimal totalEpargneDeposited;
    private BigDecimal totalEpargneWithdrawn;

    // ── Emprunts & remboursements ─────────────────────────────────────────────
    private BigDecimal totalEmpruntAmount;
    private BigDecimal totalRemboursementAmount;
    private BigDecimal totalInteretAmount;

    // ── Renfoulement & inscription ────────────────────────────────────────────
    private BigDecimal totalRenfoulementCollected;
    private BigDecimal totalRegistrationCollected;

    // ── Snapshot trésorerie mutuelle ──────────────────────────────────────────
    private BigDecimal mutuelleCash;
    private BigDecimal mutuellesSavingAmount;
    private BigDecimal mutuelleSolidarityAmount;
    private BigDecimal mutuelleRegistrationAmount;
    private BigDecimal mutuelleBorrowAmount;

    // ── Compteurs ─────────────────────────────────────────────────────────────
    private Long totalTransactions;
    private Long activeMembersCount;
}
