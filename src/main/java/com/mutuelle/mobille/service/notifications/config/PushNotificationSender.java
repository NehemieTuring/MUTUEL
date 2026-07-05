package com.mutuelle.mobille.service.notifications.config;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationSender implements NotificationSender {

    private final PushNotificationService pushNotificationService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(NotificationRequestDto req) {
        try {
            pushNotificationService.sendPush(req);
            log.info("PUSH envoyé à {}", req.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi PUSH à {} : {}", req.getEmail(), e.getMessage());
            // Option : fallback vers email si nécessaire
        }
    }
}