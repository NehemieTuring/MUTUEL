package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.service.ExerciceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exercices")
@RequiredArgsConstructor
@Tag(name = "Commissaire aux Comptes")
public class CacController {

    private final ExerciceService exerciceService;

    @PostMapping("/{id}/request-closure")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Secrétaire générale demande la clôture d'un exercice")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> requestClosure(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                exerciceService.requestClosure(id),
                "Demande de clôture soumise au commissaire aux comptes"
        ));
    }

    @PostMapping("/{id}/approve-closure")
    @PreAuthorize("hasRole('COMMISSAIRE_COMPTE')")
    @Operation(summary = "CAC approuve la clôture de l'exercice")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> approveClosure(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                exerciceService.approveClosure(id),
                "Exercice clôturé avec succès"
        ));
    }

    @PostMapping("/{id}/reject-closure")
    @PreAuthorize("hasRole('COMMISSAIRE_COMPTE')")
    @Operation(summary = "CAC rejette la demande de clôture")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> rejectClosure(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motif = body.getOrDefault("motif", "");
        return ResponseEntity.ok(ApiResponseDto.ok(
                exerciceService.rejectClosure(id, motif),
                "Demande de clôture rejetée"
        ));
    }
}
