package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.reopen.WsReopenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendReopenEvent(String email, WsReopenEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/reopen", event);
            log.debug("WS → {} : {}", email, event.getType());
        } catch (Exception e) {
            log.error("Échec envoi WS à {} : {}", email, e.getMessage());
        }
    }

    public void sendReopenEventToAll(List<String> emails, WsReopenEvent event) {
        emails.forEach(email -> sendReopenEvent(email, event));
    }
}
