package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.reopen.SessionReopenRequestResponseDTO;
import com.mutuelle.mobille.dto.reopen.WsReopenEvent;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.enums.StatusExercice;
import com.mutuelle.mobille.enums.StatusReopenRequest;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.SessionReopenRequest;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.repository.SessionReopenRequestRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionReopenService {

    private final SessionRepository sessionRepository;
    private final SessionReopenRequestRepository reopenRequestRepository;
    private final AuthUserRepository authUserRepository;
    private final SessionService sessionService;
    private final WebSocketNotificationService wsNotificationService;

    // ────────────────────────────────────────────────────────────
    //  Étape 1 : Admin initie la demande de réouverture
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SessionReopenRequestResponseDTO initiateReopen(Long sessionId, Authentication auth) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + sessionId));

        // Validations
        if (session.getStatus() != StatusSession.COMPLETED) {
            throw new IllegalStateException("Seule une session clôturée peut être réouverte");
        }

        if (session.getExercice().getStatus() != StatusExercice.IN_PROGRESS) {
            throw new IllegalStateException("L'exercice parent doit être en cours pour réouvrir la session");
        }

        // Vérifier que c'est bien la DERNIÈRE session fermée de l'exercice
        validateIsLastClosedSession(session);

        // Une seule demande PENDING à la fois par session
        if (reopenRequestRepository.existsBySessionIdAndStatus(sessionId, StatusReopenRequest.PENDING)) {
            throw new IllegalStateException("Une demande de réouverture est déjà en attente pour cette session");
        }

        // Aucune autre session IN_PROGRESS dans le même exercice
        sessionRepository.findFirstByStatus(StatusSession.IN_PROGRESS).ifPresent(s -> {
            throw new IllegalStateException(
                    "Une session est déjà en cours : '" + s.getName() + "'. Clôturez-la avant de réouvrir une autre."
            );
        });

        String adminEmail = auth.getName();

        // Récupérer les emails du président et du trésorier
        List<AuthUser> presidents = authUserRepository.findAllByRole(Role.PRESIDENT);
        List<AuthUser> tresoriers = authUserRepository.findAllByRole(Role.TRESORIER);

        if (presidents.isEmpty()) throw new IllegalStateException("Aucun président trouvé dans le système");
        if (tresoriers.isEmpty()) throw new IllegalStateException("Aucun trésorier trouvé dans le système");

        AuthUser president = presidents.get(0);
        AuthUser tresorier = tresoriers.get(0);

        // Créer la demande
        SessionReopenRequest request = SessionReopenRequest.builder()
                .session(session)
                .requestedByEmail(adminEmail)
                .status(StatusReopenRequest.PENDING)
                .presidentEmail(president.getEmail())
                .tresorierEmail(tresorier.getEmail())
                .build();
        request = reopenRequestRepository.save(request);

        // Passer la session en REOPEN_PENDING
        session.setStatus(StatusSession.REOPEN_PENDING);
        sessionRepository.save(session);

        // Notifier président et trésorier via WebSocket
        WsReopenEvent event = buildEvent(WsReopenEvent.REOPEN_REQUESTED, request,
                "Demande de réouverture de la session '" + session.getName() + "' initiée par l'admin.");

        wsNotificationService.sendReopenEventToAll(
                List.of(president.getEmail(), tresorier.getEmail()), event);

        log.info("Demande de réouverture créée pour la session '{}' par {}", session.getName(), adminEmail);
        return toDTO(request);
    }

    // ────────────────────────────────────────────────────────────
    //  Étape 2a : Président ou Trésorier approuve
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SessionReopenRequestResponseDTO approve(Long requestId, Authentication auth) {
        SessionReopenRequest request = findPendingRequest(requestId);
        String approverEmail = auth.getName();
        String approverRole  = resolveApproverRole(auth);

        applyApproval(request, approverEmail, approverRole, true);
        request = reopenRequestRepository.save(request);

        WsReopenEvent updateEvent = buildEvent(WsReopenEvent.APPROVAL_UPDATE, request,
                "Approbation enregistrée de " + approverRole + " (" + approverEmail + ")");

        // Si les deux ont approuvé → exécuter le rollback
        if (Boolean.TRUE.equals(request.getPresidentApproved())
                && Boolean.TRUE.equals(request.getTresorierApproved())) {

            request.setStatus(StatusReopenRequest.APPROVED);
            request.setResolvedAt(LocalDateTime.now());
            reopenRequestRepository.save(request);

            Session session = request.getSession();
            sessionService.rollbackOnSessionReopened(session);

            WsReopenEvent completedEvent = buildEvent(WsReopenEvent.REOPEN_COMPLETED, request,
                    "Session '" + session.getName() + "' réouverte avec succès !");

            notifyAllParties(request, completedEvent);
            log.info("Session '{}' réouverte après double approbation", session.getName());
            return toDTO(request);
        }

        // Sinon : notifier toutes les parties de la mise à jour
        notifyAllParties(request, updateEvent);
        return toDTO(request);
    }

    // ────────────────────────────────────────────────────────────
    //  Étape 2b : Président ou Trésorier refuse
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SessionReopenRequestResponseDTO reject(Long requestId, Authentication auth) {
        SessionReopenRequest request = findPendingRequest(requestId);
        String refuserEmail = auth.getName();
        String refuserRole  = resolveApproverRole(auth);

        applyApproval(request, refuserEmail, refuserRole, false);

        // Un seul refus suffit pour rejeter la demande
        request.setStatus(StatusReopenRequest.REJECTED);
        request.setResolvedAt(LocalDateTime.now());
        reopenRequestRepository.save(request);

        // Remettre la session en COMPLETED
        Session session = request.getSession();
        session.setStatus(StatusSession.COMPLETED);
        sessionRepository.save(session);

        WsReopenEvent event = buildEvent(WsReopenEvent.REOPEN_REJECTED, request,
                "Réouverture refusée par " + refuserRole + " (" + refuserEmail + "). Vous pouvez relancer une nouvelle demande.");

        notifyAllParties(request, event);
        log.info("Demande de réouverture {} rejetée par {}", requestId, refuserEmail);
        return toDTO(request);
    }

    // ────────────────────────────────────────────────────────────
    //  Admin annule la demande
    // ────────────────────────────────────────────────────────────

    @Transactional
    public SessionReopenRequestResponseDTO cancel(Long requestId, Authentication auth) {
        SessionReopenRequest request = findPendingRequest(requestId);

        request.setStatus(StatusReopenRequest.CANCELLED);
        request.setResolvedAt(LocalDateTime.now());
        reopenRequestRepository.save(request);

        // Remettre la session en COMPLETED
        Session session = request.getSession();
        session.setStatus(StatusSession.COMPLETED);
        sessionRepository.save(session);

        WsReopenEvent event = buildEvent(WsReopenEvent.REOPEN_CANCELLED, request,
                "Demande de réouverture annulée par l'admin.");

        wsNotificationService.sendReopenEventToAll(
                List.of(request.getPresidentEmail(), request.getTresorierEmail()), event);

        log.info("Demande de réouverture {} annulée par {}", requestId, auth.getName());
        return toDTO(request);
    }

    // ────────────────────────────────────────────────────────────
    //  Lecture
    // ────────────────────────────────────────────────────────────

    public SessionReopenRequestResponseDTO getRequest(Long requestId) {
        return reopenRequestRepository.findById(requestId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée : " + requestId));
    }

    public SessionReopenRequestResponseDTO getPendingForSession(Long sessionId) {
        return reopenRequestRepository
                .findBySessionIdAndStatus(sessionId, StatusReopenRequest.PENDING)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Aucune demande en attente pour la session " + sessionId));
    }

    public List<SessionReopenRequestResponseDTO> getHistoryForSession(Long sessionId) {
        return reopenRequestRepository
                .findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────
    //  Méthodes internes
    // ────────────────────────────────────────────────────────────

    private SessionReopenRequest findPendingRequest(Long requestId) {
        SessionReopenRequest request = reopenRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée : " + requestId));
        if (request.getStatus() != StatusReopenRequest.PENDING) {
            throw new IllegalStateException(
                    "Cette demande n'est plus en attente (statut : " + request.getStatus() + ")");
        }
        return request;
    }

    private String resolveApproverRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.equals("ROLE_PRESIDENT") || a.equals("ROLE_TRESORIER"))
                .findFirst()
                .map(a -> a.replace("ROLE_", ""))
                .orElseThrow(() -> new IllegalStateException("Rôle non autorisé pour cette action"));
    }

    private void applyApproval(SessionReopenRequest request,
                               String email, String role, boolean approved) {
        LocalDateTime now = LocalDateTime.now();
        if ("PRESIDENT".equals(role)) {
            if (!email.equals(request.getPresidentEmail())) {
                throw new IllegalStateException("Seul le président désigné peut approuver/refuser");
            }
            if (request.getPresidentApproved() != null) {
                throw new IllegalStateException("Le président a déjà donné sa réponse");
            }
            request.setPresidentApproved(approved);
            request.setPresidentApprovedAt(now);
        } else {
            if (!email.equals(request.getTresorierEmail())) {
                throw new IllegalStateException("Seul le trésorier désigné peut approuver/refuser");
            }
            if (request.getTresorierApproved() != null) {
                throw new IllegalStateException("Le trésorier a déjà donné sa réponse");
            }
            request.setTresorierApproved(approved);
            request.setTresorierApprovedAt(now);
        }
    }

    private void validateIsLastClosedSession(Session session) {
        List<Session> completedSessions = sessionRepository
                .findByExerciceIdAndStatus(session.getExercice().getId(), StatusSession.COMPLETED);

        boolean isLast = completedSessions.stream()
                .filter(s -> s.getEndDate() != null)
                .max((a, b) -> a.getEndDate().compareTo(b.getEndDate()))
                .map(last -> last.getId().equals(session.getId()))
                .orElse(false);

        if (!isLast) {
            throw new IllegalStateException(
                    "Seule la dernière session clôturée peut être réouverte");
        }
    }

    private void notifyAllParties(SessionReopenRequest request, WsReopenEvent event) {
        List<String> emails = List.of(
                request.getRequestedByEmail(),
                request.getPresidentEmail(),
                request.getTresorierEmail()
        );
        wsNotificationService.sendReopenEventToAll(emails, event);
    }

    private WsReopenEvent buildEvent(String type, SessionReopenRequest request, String message) {
        return WsReopenEvent.builder()
                .type(type)
                .requestId(request.getId())
                .sessionId(request.getSession().getId())
                .sessionName(request.getSession().getName())
                .message(message)
                .presidentApproved(request.getPresidentApproved())
                .tresorierApproved(request.getTresorierApproved())
                .build();
    }

    public SessionReopenRequestResponseDTO toDTO(SessionReopenRequest r) {
        return SessionReopenRequestResponseDTO.builder()
                .id(r.getId())
                .sessionId(r.getSession().getId())
                .sessionName(r.getSession().getName())
                .requestedByEmail(r.getRequestedByEmail())
                .requestedAt(r.getRequestedAt())
                .status(r.getStatus())
                .presidentApproved(r.getPresidentApproved())
                .presidentApprovedAt(r.getPresidentApprovedAt())
                .tresorierApproved(r.getTresorierApproved())
                .tresorierApprovedAt(r.getTresorierApprovedAt())
                .resolvedAt(r.getResolvedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
