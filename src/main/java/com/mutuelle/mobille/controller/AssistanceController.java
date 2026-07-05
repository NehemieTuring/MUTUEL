package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.assistance.*;
import com.mutuelle.mobille.models.Assistance;
import com.mutuelle.mobille.service.AssistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assistances")
@RequiredArgsConstructor
@Tag(name = "Gestion des Assistances")
public class AssistanceController {

    private final AssistanceService assistanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()") // ou hasRole("ADMIN") / "TRESORIER" selon ton besoin
    @Operation(summary = "Lister les demandes d'assistance avec filtres")
    public ResponseEntity<ApiResponseDto<List<AssistanceResponseDto>>> getAllAssistances(
            @RequestParam(required = false) Long typeAssistanceId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction dir = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortParams[0]));

        Page<AssistanceResponseDto> result = assistanceService.getAssistancesFiltered(
                typeAssistanceId,
                memberId,
                sessionId,
                fromDate,
                toDate,
                pageable
        );

        return ResponseEntity.ok(ApiResponseDto.okPaged(
                result.getContent(),
                "Liste des assistances récupérée avec succès",
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        ));
    }


    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer la liste de tous les types d'assistance")
    public ResponseEntity<ApiResponseDto<List<TypeAssistanceResponseDto>>> getAllTypes() {
        List<TypeAssistanceResponseDto> types = assistanceService.getAllTypeAssistances();
        return ResponseEntity.ok(ApiResponseDto.ok(types, "Liste des types d'assistance récupérée avec succès"));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau type d'assistance")
    public ResponseEntity<ApiResponseDto<TypeAssistanceResponseDto>> createType(
            @Valid @RequestBody CreateTypeAssistanceDto request) {

        TypeAssistanceResponseDto created = assistanceService.createTypeAssistance(request);
        return ResponseEntity.ok(ApiResponseDto.ok(created, "Type d'assistance créé avec succès"));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un type d'assistance")
    public ResponseEntity<ApiResponseDto<TypeAssistanceResponseDto>> updateType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTypeAssistanceDto request) {

        TypeAssistanceResponseDto updated = assistanceService.updateTypeAssistance(id, request);
        return ResponseEntity.ok(ApiResponseDto.ok(updated, "Type d'assistance mis à jour avec succès"));
    }

    // === Gestion des assistances (demandes) ===

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer une nouvelle demande d'assistance")
    public ResponseEntity<ApiResponseDto<AssistanceResponseDto>> createAssistance(
            @Valid @RequestBody CreateAssistanceDto request) {

        AssistanceResponseDto created = assistanceService.createAssistance(request);
        return ResponseEntity.ok(ApiResponseDto.ok(created, "Demande d'assistance créée avec succès"));
    }
}