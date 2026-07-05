package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.reopen.SessionReopenRequestResponseDTO;
import com.mutuelle.mobille.service.SessionReopenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Réouverture de session")
public class SessionReopenController {

    private final SessionReopenService reopenService;

    @PostMapping("/{sessionId}/reopen/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initier une demande de réouverture de session (admin uniquement)")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> initiateReopen(
            @PathVariable Long sessionId,
            Authentication auth) {
        SessionReopenRequestResponseDTO response = reopenService.initiateReopen(sessionId, auth);
        return ResponseEntity.ok(ApiResponseDto.ok(response,
                "Demande de réouverture initiée — en attente du président et du trésorier"));
    }

    @PostMapping("/reopen/{requestId}/approve")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'TRESORIER')")
    @Operation(summary = "Approuver une demande de réouverture (président ou trésorier)")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> approve(
            @PathVariable Long requestId,
            Authentication auth) {
        SessionReopenRequestResponseDTO response = reopenService.approve(requestId, auth);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Approbation enregistrée"));
    }

    @PostMapping("/reopen/{requestId}/reject")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'TRESORIER')")
    @Operation(summary = "Refuser une demande de réouverture (président ou trésorier)")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> reject(
            @PathVariable Long requestId,
            Authentication auth) {
        SessionReopenRequestResponseDTO response = reopenService.reject(requestId, auth);
        return ResponseEntity.ok(ApiResponseDto.ok(response,
                "Demande refusée — l'admin peut relancer une nouvelle demande"));
    }

    @PostMapping("/reopen/{requestId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Annuler une demande de réouverture (admin uniquement)")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> cancel(
            @PathVariable Long requestId,
            Authentication auth) {
        SessionReopenRequestResponseDTO response = reopenService.cancel(requestId, auth);
        return ResponseEntity.ok(ApiResponseDto.ok(response, "Demande annulée"));
    }

    @GetMapping("/reopen/{requestId}")
    @Operation(summary = "Récupérer une demande de réouverture par ID")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> getRequest(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponseDto.ok(reopenService.getRequest(requestId), "Demande trouvée"));
    }

    @GetMapping("/{sessionId}/reopen/pending")
    @Operation(summary = "Récupérer la demande en attente pour une session")
    public ResponseEntity<ApiResponseDto<SessionReopenRequestResponseDTO>> getPending(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                reopenService.getPendingForSession(sessionId), "Demande en attente trouvée"));
    }

    @GetMapping("/{sessionId}/reopen/history")
    @Operation(summary = "Historique des demandes de réouverture pour une session")
    public ResponseEntity<ApiResponseDto<List<SessionReopenRequestResponseDTO>>> getHistory(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponseDto.ok(
                reopenService.getHistoryForSession(sessionId), "Historique récupéré"));
    }
}
