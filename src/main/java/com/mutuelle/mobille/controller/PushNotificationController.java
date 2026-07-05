package com.mutuelle.mobille.controller;


import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.dto.notifications.PushTestDto;
import com.mutuelle.mobille.dto.notifications.PushTokenDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import com.mutuelle.mobille.enums.TemplateMailsName;
import com.mutuelle.mobille.service.notifications.config.NotificationService;
import com.mutuelle.mobille.service.notifications.config.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/notifications/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;
    private final NotificationService notificationService;

    @PostMapping("/token")
    @Operation(summary = "Enregistrer un nouveau token push pour l'utilisateur connecté (support multi-appareils)")
    public ResponseEntity<ApiResponseDto<Void>> registerToken(@Valid @RequestBody PushTokenDto dto) {
        pushNotificationService.registerToken(dto);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Token push enregistré avec succès"));
    }

    @PostMapping("/test")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envoyer une notification push de TEST à TOUS les appareils d'un utilisateur donné (admin only)")
    public ResponseEntity<ApiResponseDto<Void>> testPushToUser(@Valid @RequestBody PushTestDto dto) {
        pushNotificationService.sendTestPushToUser(dto.getUserId(), dto.getTitle(), dto.getMessage());
        return ResponseEntity.ok(ApiResponseDto.ok(null,
                "Notification de test envoyée à l'utilisateur ID " + dto.getUserId() + " (tous appareils)"));
    }

    @PostMapping("/test/mail")
    @Operation(summary = "Envoyer une notification mail de TEST")
    public ResponseEntity<ApiResponseDto<Void>> testMail(
            @Valid @RequestBody TestMailRequest req) {

        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("userName", "Test User");
            vars.put("currentTime", LocalDateTime.now().toString());
            // tu peux ajouter d'autres variables si besoin
            // vars.put("customMessage", req.getBody());

            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .email(req.email())           // ← on prend l'email reçu
                    .title(req.subject())
                    .templateName(TemplateMailsName.PLAFOND_DEPASSE_ADMIN)
                    .variables(vars)
                    .channels(Set.of(NotificationChannel.EMAIL))  // ← uniquement email, pas de push
                    .message(req.body())          // fallback si template plante
                    .build();

            notificationService.sendNotification(notification);

            String message = "Email de test envoyé avec succès à : " + req.email();

            return ResponseEntity.ok(
                    ApiResponseDto.ok(null, message)
            );

        } catch (Exception e) {
            // log l'erreur pour debug
            // logger.error("Échec envoi email test", e);  ← décommente si tu as un logger

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erreur lors de l'envoi : " + e.getMessage()));
        }
    }

    record TestMailRequest(
            @Email @NotBlank String email,
            @NotBlank String subject,
            @NotBlank String body
    ) {}
}