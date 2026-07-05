package com.mutuelle.mobille.dto.bilan;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberSessionBilanDTO {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private Long id;
    private Long memberId;
    private String memberFirstname;
    private String memberLastname;
    private Long sessionId;
    private String sessionName;
    private String exerciceName;
    private LocalDateTime sessionStartDate;
    private LocalDateTime sessionEndDate;
    private LocalDateTime createdAt;

    // ── Versements du membre (argent donné à la mutuelle) ─────────────────────
    private BigDecimal solidaritePaid;
    private BigDecimal epargneDeposited;
    private BigDecimal registrationPaid;
    private BigDecimal renfoulementPaid;
    private BigDecimal remboursementAmount;

    // ── Décaissements / Dettes (argent reçu ou dû) ────────────────────────────
    private BigDecimal epargneWithdrawn;
    private BigDecimal empruntAmount;
    private BigDecimal interetAmount;
    private BigDecimal assistanceReceived;
    private BigDecimal agapeShare;

    // ── Calculés ──────────────────────────────────────────────────────────────
    private BigDecimal totalVerse;      // somme des versements
    private BigDecimal totalRecu;       // somme des décaissements
    private BigDecimal netSession;      // totalVerse - totalRecu

    // ── Snapshot du compte à la clôture ───────────────────────────────────────
    private BigDecimal snapshotSavingAmount;
    private BigDecimal snapshotBorrowAmount;
    private BigDecimal snapshotUnpaidSolidarity;
    private BigDecimal snapshotUnpaidRegistration;
    private BigDecimal snapshotUnpaidRenfoulement;
}
