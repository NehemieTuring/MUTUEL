package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.transaction.EmpruntRequestDTO;
import com.mutuelle.mobille.dto.transaction.RemboursementRequestDTO;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.service.EmpruntService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller métier pour la gestion des emprunts et remboursements
 * (sans impacter le module Transaction global)
 */
@RestController
@RequestMapping("/api/emprunts")
@RequiredArgsConstructor
@Tag(name = "Emprunts")
public class EmpruntController {

    private final EmpruntService empruntService;

    // ─────────────────────────────────────────────
    // EMPRUNT
    // ─────────────────────────────────────────────

    @PostMapping("/emprunt")
    @Operation(summary = "Effectuer un emprunt")
    public ResponseEntity<ApiResponseDto<Void>> emprunter(
            @Valid @RequestBody EmpruntRequestDTO request
    ) {
        empruntService.emprunter(
                request.getMemberId(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Emprunt effectué avec succès")
        );
    }


    // ─────────────────────────────────────────────
    // REMBOURSEMENT
    // ─────────────────────────────────────────────

    @PostMapping("/remboursement")
    @Operation(summary = "Effectuer un remboursement d'emprunt")
    public ResponseEntity<ApiResponseDto<Void>> rembourser(
            @Valid @RequestBody RemboursementRequestDTO request
    ) {
        empruntService.rembourser(
                request.getMemberId(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Remboursement effectué avec succès")
        );
    }

}
