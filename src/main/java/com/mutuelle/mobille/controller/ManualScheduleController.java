package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.SessionService;
import com.mutuelle.mobille.service.schedules.FinancialSchedules;
import com.mutuelle.mobille.service.schedules.StatusSchedules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

@RestController
@RequestMapping("/api/shedules")
@RequiredArgsConstructor
@Tag(name = "Shedules manuels", description = "Déclenchement manuel des tâches planifiées (pour tests, debug, urgence)")
public class ManualScheduleController {

    private final StatusSchedules statusSchedules;
    private final FinancialSchedules financialSchedules;
    private final SessionRepository sessionRepository;

    // -------------------------------------------------------------------------
    //                  STATUTS (sessions + exercices)
    // -------------------------------------------------------------------------

//    @PostMapping("/status/synchronize-all")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Exécute manuellement la synchronisation complète des statuts",
//            description = "Lance synchronizeAllStatuses() → termine les sessions/exercices expirés + démarre les suivants si possible"
//    )
//    public ResponseEntity<ApiResponseDto<String>> triggerSynchronizeAllStatuses() {
//        try {
//            statusSchedules.synchronizeAllStatuses();
//            return ResponseEntity.ok(
//                    ApiResponseDto.ok("Synchronisation des statuts terminée avec succès", "Opération manuelle effectuée")
//            );
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(
//                    ApiResponseDto.error("Échec de la synchronisation manuelle des statuts: "+ e.getMessage())
//            );
//        }
//    }
//    /*
//    @PostMapping("/status/sessions/terminate-expired")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Termine manuellement toutes les sessions expirées sans renfoulement etc ...",
//            description = "Exécute uniquement la partie 'clôture des sessions dépassées'"
//    )
//    public ResponseEntity<ApiResponseDto<String>> triggerTerminateExpiredSessions() {
//        try {
//            // On passe un fake now() très lointain pour forcer la détection de toutes les expirées
//            statusSchedules.synchronizeSessions(now().plusYears(10));
//            return ResponseEntity.ok(
//                    ApiResponseDto.ok("Vérification et clôture des sessions expirées terminée", "Opération manuelle effectuée")
//            );
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(
//                    ApiResponseDto.error("Échec lors de la clôture manuelle des sessions: "+ e.getMessage())
//            );
//        }
//    }*/
//    /*
//    @PostMapping("/status/sessions/start-pending")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Démarre manuellement les sessions dues (si aucune en cours)",
//            description = "Exécute uniquement la partie démarrage des sessions planifiées et dues"
//    )
//    public ResponseEntity<ApiResponseDto<String>> triggerStartPendingSessions() {
//        try {
//            // On force la vérification avec la date actuelle
//            statusSchedules.synchronizeSessions(now());
//            return ResponseEntity.ok(
//                    ApiResponseDto.ok("Tentative de démarrage des sessions dues terminée", "Opération manuelle effectuée")
//            );
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(
//                    ApiResponseDto.error("Échec lors du démarrage manuel des sessions: "+ e.getMessage())
//            );
//        }
//    }*/
//
//    // -------------------------------------------------------------------------
//    //                  INTÉRÊTS TRIMESTRIELS EMPRUNTS
//    // -------------------------------------------------------------------------
//
//    @PostMapping("/financial/interests/trimestrial")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Exécute manuellement le calcul et la redistribution des intérêts trimestriels",
//            description = "Lance processTrimestrialInterests() → utile pour tests ou rattrapage"
//    )
//    public ResponseEntity<ApiResponseDto<String>> triggerTrimestrialInterests() {
//        try {
//            financialSchedules.processTrimestrialInterests();
//            return ResponseEntity.ok(
//                    ApiResponseDto.ok("Calcul et redistribution des intérêts trimestriels terminés", "Opération manuelle effectuée")
//            );
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(
//                    ApiResponseDto.error("Échec du traitement manuel des intérêts trimestriels: "+ e.getMessage())
//            );
//        }
//    }
//
//    // -------------------------------------------------------------------------
//    //                  Exemple de point d'entrée "tout lancer" (optionnel)
//    // -------------------------------------------------------------------------
//
//    @PostMapping("/trigger-all")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Exécute TOUTES les tâches planifiées manuellement (attention !)",
//            description = "Lance la synchro statuts + intérêts trimestriels"
//    )
//    public ResponseEntity<ApiResponseDto<String>> triggerAllManual() {
//        try {
//            statusSchedules.synchronizeAllStatuses();
//            financialSchedules.processTrimestrialInterests();
//            return ResponseEntity.ok(
//                    ApiResponseDto.ok("Toutes les tâches planifiées ont été exécutées manuellement", "Opération complète terminée")
//            );
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(
//                    ApiResponseDto.error("Échec lors de l'exécution complète manuelle : "+ e.getMessage())
//            );
//        }
//    }
}