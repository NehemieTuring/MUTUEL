package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.service.SolidariteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/solidarite")
@RequiredArgsConstructor
@Tag(name = "Solidarité")
public class SolidariteController {

    private final SolidariteService solidariteService;

    // ─────────────────────────────────────────────
    // DTO DE REQUÊTE
    // ─────────────────────────────────────────────
    public static class PaiementRequest {

        private Long membreId;
        private BigDecimal montant;
        private Long sessionId;

        public Long getMembreId() {
            return membreId;
        }

        public void setMembreId(Long membreId) {
            this.membreId = membreId;
        }

        public BigDecimal getMontant() {
            return montant;
        }

        public void setMontant(BigDecimal montant) {
            this.montant = montant;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }
    }

    // PAIEMENT DE LA SOLIDARITÉ
    @PostMapping("/payer")
    @Operation(summary = "Enregistrer un paiement de solidarité")
    public ResponseEntity<ApiResponseDto<Void>> payerSolidarite(
            @Valid @RequestBody PaiementRequest request) {
        solidariteService.paySolidarity(
                request.getMembreId(),
                request.getMontant(),
                request.getSessionId());

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Paiement de solidarité enregistré avec succès"));
    }

    // ─────────────────────────────────────────────
    // HISTORIQUE DES PAIEMENTS
    // ─────────────────────────────────────────────
    @GetMapping("/membre/{id}/historique")
    @Operation(summary = "Historique des paiements de solidarité d'un membre")
    public ResponseEntity<ApiResponseDto<List<Transaction>>> historiqueSolidarite(
            @PathVariable Long id) {
        List<Transaction> transactions = solidariteService.getSolidarityHistory(id);

        return ResponseEntity.ok(
                ApiResponseDto.ok(transactions, "Historique récupéré avec succès"));
    }

    // ─────────────────────────────────────────────
    // TOTAL PAYÉ
    // ─────────────────────────────────────────────
    @GetMapping("/membre/{id}/total")
    @Operation(summary = "Total payé en solidarité par un membre")
    public ResponseEntity<ApiResponseDto<BigDecimal>> totalSolidarite(
            @PathVariable Long id) {
        BigDecimal total = solidariteService.getTotalSolidarityPaid(id);

        return ResponseEntity.ok(
                ApiResponseDto.ok(total, "Total de solidarité récupéré avec succès"));
    }
}
