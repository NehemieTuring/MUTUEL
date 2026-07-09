package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.caisse.FraisGestionWithdrawalRequestDto;
import com.mutuelle.mobille.dto.caisse.FraisGestionWithdrawalResponseDto;
import com.mutuelle.mobille.service.FraisGestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caisse-inscription/frais-gestion")
@RequiredArgsConstructor
@Tag(name = "Frais de gestion", description = "Retraits SG sur la caisse inscription")
public class FraisGestionController {

    private final FraisGestionService fraisGestionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retirer des frais de gestion de la caisse inscription (session ouverte requise)")
    public ResponseEntity<ApiResponseDto<FraisGestionWithdrawalResponseDto>> withdraw(
            @Valid @RequestBody FraisGestionWithdrawalRequestDto request) {
        FraisGestionWithdrawalResponseDto result = fraisGestionService.withdraw(request);
        return ResponseEntity.ok(ApiResponseDto.ok(result, "Retrait enregistré"));
    }

    @GetMapping("/exercice/{exerciceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESIDENT', 'TRESORIER', 'COMMISSAIRE_COMPTE')")
    @Operation(summary = "Lister les retraits de frais de gestion d'un exercice")
    public ResponseEntity<ApiResponseDto<List<FraisGestionWithdrawalResponseDto>>> listByExercice(
            @PathVariable Long exerciceId) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                fraisGestionService.listByExercice(exerciceId),
                "Retraits récupérés"));
    }
}
