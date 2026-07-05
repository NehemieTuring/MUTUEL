package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.config.ConfigMutuelleRequestDto;
import com.mutuelle.mobille.dto.config.ConfigMutuelleResponseDto;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.service.AuthService;
import com.mutuelle.mobille.service.MutuelleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mutuelle.mobille.service.MemberService;

import java.util.Optional;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Configuration Mutuelle", description = "Gestion des paramètres globaux de la mutuelle")
@RequiredArgsConstructor
public class ConfigController {

    private final MutuelleConfigService configService;
    private final AuthService authService;
    private final MemberService memberService;


    /**
     * Récupérer la configuration actuelle
     */
    @GetMapping("/current")
    @Operation(summary = "Obtenir la configuration actuelle de la mutuelle")
    public ResponseEntity<ApiResponseDto<ConfigMutuelleResponseDto>> getCurrentConfig() {
        MutuelleConfig config = configService.getCurrentConfig();
        return ResponseEntity.ok(ApiResponseDto.ok(
                new ConfigMutuelleResponseDto(config),
                "Configuration récupérée avec succès"
        ));
    }

    /**
     * Mettre à jour la configuration globale
     */
    @PatchMapping("/current/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Mettre à jour partiellement la configuration de la mutuelle",
            description = "Seuls les champs fournis dans le body sont modifiés. Réservé aux administrateurs."
    )
    public ResponseEntity<ApiResponseDto<ConfigMutuelleResponseDto>> partialUpdateConfig(
            @Valid @RequestBody ConfigMutuelleRequestDto request ) {

        MutuelleConfig current = configService.getCurrentConfig();
        boolean hasChanges = false;

        if (request.getRegistrationFeeAmount() != null) {
            current.setRegistrationFeeAmount(request.getRegistrationFeeAmount());
            hasChanges = true;
        }

        if (request.getSolidarityFeeAmount() != null) {
            current.setSolidarityFeeAmount(request.getSolidarityFeeAmount());
            hasChanges = true;
        }

        if (request.getLoanInterestRatePercent() != null) {
            current.setLoanInterestRatePercent(request.getLoanInterestRatePercent());
            hasChanges = true;
        }

        // --- Nouveaux champs ---
        if (request.getLoanPenaltyFixedAmount() != null) {
            current.setLoanPenaltyFixedAmount(request.getLoanPenaltyFixedAmount());
            hasChanges = true;
        }

        if (request.getLoanPenaltySessionThreshold() != null) {
            current.setLoanPenaltySessionThreshold(request.getLoanPenaltySessionThreshold());
            hasChanges = true;
        }

        if (request.getInsolvencyThreshold() != null) {
            current.setInsolvencyThreshold(request.getInsolvencyThreshold());
            hasChanges = true;
        }

        if (!hasChanges) {
            return ResponseEntity.ok(ApiResponseDto.ok(
                    new ConfigMutuelleResponseDto(current),
                    "Aucune modification fournie → configuration inchangée"
            ));
        }

        AuthUser currentUser = authService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        // On passe l'objet 'current' modifié.
        // Note: Assurez-vous que votre service.updateConfig accepte bien les nouveaux champs
        MutuelleConfig saved = configService.updateConfig(current, currentUser.getRole().getValue());

        return ResponseEntity.ok(ApiResponseDto.ok(
                new ConfigMutuelleResponseDto(saved),
                "Configuration mise à jour avec succès"
        ));
    }

    @PostMapping("/admin/recalculate-statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<String>> recalculateAllStatuses() {
        memberService.recalculateAllMemberStatuses();
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Tous les statuts ont été recalculés"));
    }

}