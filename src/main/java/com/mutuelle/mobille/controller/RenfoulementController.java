package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.renfoulement.RenfoulementHistoryResponseDto;
import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.service.ExerciceService;
import com.mutuelle.mobille.service.RenfoulementService;
import com.mutuelle.mobille.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Renfoulement")
@RestController
@RequestMapping("/api/renfoulements")
@RequiredArgsConstructor
public class RenfoulementController {

    private final RenfoulementService renfoulementService;
    private final ExerciceService exerciceService;

    @GetMapping()
    @Operation(summary = "Liste des renfoulements passés globaux + simulation courant")
    public ResponseEntity<ApiResponseDto<RenfoulementHistoryResponseDto>> getGlobalRenfoulementHistory() {

        RenfoulementHistoryResponseDto response = renfoulementService.getGlobalRenfoulementHistory();
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Historique renfoulement global"));
    }
}