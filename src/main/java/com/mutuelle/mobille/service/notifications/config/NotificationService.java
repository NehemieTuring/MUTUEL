package com.mutuelle.mobille.service.notifications.config;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import com.mutuelle.mobille.enums.TemplateMailsName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final List<NotificationSender> senders;  // toutes les implémentations auto-détectées

    // Méthode la plus utilisée : envoi vers les canaux souhaités
    public void sendNotification(NotificationRequestDto request) {
        Set<NotificationChannel> targetChannels = request.getChannels();

        // Si aucun canal spécifié → règle par défaut (ex: email + push si disponible)
        if (targetChannels == null || targetChannels.isEmpty()) {
            targetChannels = Set.of(NotificationChannel.EMAIL, NotificationChannel.PUSH);
        }

        Map<NotificationChannel, NotificationSender> senderMap = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, s -> s));

        for (NotificationChannel channel : targetChannels) {
            NotificationSender sender = senderMap.get(channel);
            if (sender != null) {
                try {
                    sender.send(request);
                } catch (Exception e) {
                    // Log + éventuellement fallback vers un autre canal
                    // ex: si PUSH échoue → fallback EMAIL
                }
            } else {
                // Canal non implémenté → log warning
            }
        }
    }

    // Méthode de confort pour welcome (comme avant)
    public void sendWelcome(
            String email,
            String prenom,
            String numeroAdherent,
            String activationLink) {

        Map<String, Object> vars = Map.of(
                "prenom", prenom,
                "numeroAdherent", numeroAdherent,
                "activationLink", activationLink
        );

        NotificationRequestDto req = NotificationRequestDto.builder()
                .email(email)
                .title("Bienvenue dans la Mutuelle ENSPY !")
                .templateName(TemplateMailsName.WELCOME)
                .variables(vars)
                .channels(Set.of(NotificationChannel.EMAIL))  // ou .of(EMAIL, PUSH) plus tard
                .build();

        sendNotification(req);
    }

    // Exemple futur : envoi critique → email + push
    public void sendCriticalAlert(String email, String message) {
        NotificationRequestDto req = NotificationRequestDto.builder()
                .email(email)
                .title("Action requise – Mutuelle ENSPY")
                .message(message)
                .channels(Set.of(NotificationChannel.EMAIL, NotificationChannel.PUSH))
                .build();

        sendNotification(req);
    }
}