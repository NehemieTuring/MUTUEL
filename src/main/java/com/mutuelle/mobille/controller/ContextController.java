package com.mutuelle.mobille.controller;


import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.CurrentContextResponseDTO;
import com.mutuelle.mobille.service.AssistanceService;
import com.mutuelle.mobille.service.CurrentContextService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/context")
@RequiredArgsConstructor
public class ContextController {

    private final CurrentContextService currentContextService;

    @GetMapping
    @Operation(summary = "Récupère le contexte courant pour l'interface utilisateur")
    public ResponseEntity<ApiResponseDto<CurrentContextResponseDTO>> getCurrentContext() {
        CurrentContextResponseDTO context = currentContextService.buildCurrentContext();

        return ResponseEntity.ok(
                ApiResponseDto.ok(context, "Compte du membre récupéré avec succès")
        );
    }
}