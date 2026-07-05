package com.mutuelle.mobille.service.notifications.config;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRequestDto req) {
        if (req.getTemplateName() == null) {
            emailService.sendSimpleEmail(
                    req.getEmail(),
                    req.getTitle(),
                    req.getMessage()
            );
            return;
        }

        // 1. Préparation du contexte Thymeleaf
        Context context = new Context();

        // 2. Injection sécurisée des variables personnalisées
        if (req.getVariables() != null) {
            req.getVariables().forEach(context::setVariable);
        }

        // 3. Injection automatique des champs de base (pratique pour le template)
        // Cela permet d'utiliser [[${title}]] ou [[${message}]] directement dans le HTML
        context.setVariable("title", req.getTitle());
        context.setVariable("message", req.getMessage());

        // 4. Variables globales (Marque, URLs, etc.)
        Map<String, Object> globalVars = new HashMap<>();
        globalVars.put("appName", "Mutuelle ENSPY");
        globalVars.put("logoUrl", "https://api.dicebear.com/7.x/initials/svg?seed=Mutuelle+ENSPY&backgroundColor=0056b3&fontColor=ffffff&fontSize=35");
        context.setVariables(globalVars);

        // 5. Génération et envoi
        String html = templateEngine.process("email/" + req.getTemplateName(), context);

        emailService.sendHtmlEmail(
                req.getEmail(),
                req.getTitle(),
                html,
                null,
                null
        );
    }
}