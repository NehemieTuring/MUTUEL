package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceRequestDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.service.ExerciceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercices")
@RequiredArgsConstructor
@Tag(name = "Exercices (S'étend souvent sur 1 an)")
public class ExcerciceController {

    private final ExerciceService exerciceService;

    @GetMapping
    @Operation(summary = "Lister tous les exercices (accessible à tous les utilisateurs authentifiés)")
    public ResponseEntity<ApiResponseDto<List<ExerciceResponseDTO>>> getAllExercices() {
        List<ExerciceResponseDTO> exercices = exerciceService.getAllExercices();
        return ResponseEntity.ok(ApiResponseDto.ok(exercices, "Liste des exercices récupérée"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un exercice par ID (accessible à tous les utilisateurs authentifiés)")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> getExerciceById(@PathVariable Long id) {
        ExerciceResponseDTO exercice = exerciceService.getExerciceById(id);
        return ResponseEntity.ok(ApiResponseDto.ok(exercice, "Exercice trouvé"));
    }

    @GetMapping("/current")
    @Operation(summary = "Récupérer l'exercice en cours")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> getCurrentExercice() {
        return exerciceService.getCurrentExerciceDTO()
                .map(dto -> ResponseEntity.ok(ApiResponseDto.ok(dto, "Exercice en cours trouvé")))
                .orElseThrow(() -> new IllegalStateException("Aucun exercice en cours"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouvel exercice (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> createExercice(@Valid @RequestBody ExerciceRequestDTO request) {
        ExerciceResponseDTO response = exerciceService.createExercice(request);
        return ResponseEntity.status(201).body(ApiResponseDto.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un exercice (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> updateExercice(
            @PathVariable Long id,
            @Valid @RequestBody ExerciceRequestDTO request) {
        ExerciceResponseDTO response = exerciceService.updateExercice(id, request);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Exercice mis à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un exercice (administrateurs uniquement)")
    public ResponseEntity<ApiResponseDto<Void>> deleteExercice(@PathVariable Long id) {
        exerciceService.deleteExercice(id);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Exercice supprimé avec succès"));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Démarrer un exercice")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> startExercice(@PathVariable Long id) {
        ExerciceResponseDTO response = exerciceService.startExercice(id);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Exercice démarré avec succès"));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clôturer un exercice")
    public ResponseEntity<ApiResponseDto<ExerciceResponseDTO>> closeExercice(@PathVariable Long id) {
        ExerciceResponseDTO response = exerciceService.closeExercice(id);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Exercice clôturé avec succès"));
    }

}