package com.mutuelle.mobille.service.schedules;

import com.mutuelle.mobille.enums.StatusExercice;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.ExerciceService;
import com.mutuelle.mobille.service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class StatusSchedules {

    private final ExerciceRepository exerciceRepository;
    private final SessionRepository sessionRepository;
    private final ExerciceService exerciceService;
    private final SessionService sessionService;

//    private final Clock clock;

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

//    @Scheduled(cron = "0 30 6,12,18 * * ?")  //06h30, 12h30, 18h30
//    @Transactional
//    public void synchronizeAllStatuses() {
//        LocalDateTime currentTime = now();
//        log.info("Début synchronisation statuts - {}", currentTime);
//
//        try {
//            //synchronizeSessions(currentTime);
//            //synchronizeExercices(currentTime);
//        } catch (Exception e) {
//            log.error("Erreur critique lors de la synchronisation des statuts", e);
//            // Option : envoyer alerte (email/slack) ici si très grave
//        }
//
//        log.debug("Fin synchronisation statuts");
//    }
    /*
    public void synchronizeSessions(LocalDateTime now) {

        // Étape 1 : TERMINER les sessions expirées (priorité absolue)
        List<Session> expired = sessionRepository.findByStatusAndEndDateLessThan(
                StatusSession.IN_PROGRESS, now);

        for (Session s : expired) {
            try {
                if (s.getStatus() != StatusSession.IN_PROGRESS) continue; // sécurité

                s.setStatus(StatusSession.COMPLETED);
                sessionRepository.save(s);
                sessionService.onSessionEnded(s);
                log.info("Session terminée automatiquement → {} (id: {})", s.getName(), s.getId());
            } catch (Exception e) {
                log.error("Échec clôture session {} : {}", s.getId(), e.getMessage(), e);
                // Ne pas stopper les autres → continue
            }
        }

        // Étape 2 : DÉMARRER les sessions dues (seulement si aucune en cours)
        if (sessionRepository.findFirstByStatus(StatusSession.IN_PROGRESS).isEmpty()) {
            List<Session> toStart = sessionRepository.findByStatusAndStartDateLessThanEqual(
                    StatusSession.PLANNED, now);

            for (Session s : toStart) {
                try {
                    if (s.getStatus() != StatusSession.PLANNED) continue;

                    s.setStatus(StatusSession.IN_PROGRESS);
                    sessionRepository.save(s);
                    sessionService.applySolidarityToAllMembers(s);
                    log.info("Session démarrée automatiquement → {} (id: {})", s.getName(), s.getId());
                } catch (Exception e) {
                    log.error("Échec démarrage session {} : {}", s.getId(), e.getMessage(), e);
                }
            }
        } else {
            log.warn("Démarrage session bloqué : une session est déjà IN_PROGRESS");
        }
    }*/

    /*private void synchronizeExercices(LocalDateTime now) {
        // Étape 1 : TERMINER les exercices expirés (priorité)
        List<Exercice> expired = exerciceRepository.findByStatusAndEndDateLessThan(
                StatusExercice.IN_PROGRESS, now);
        for (Exercice ex : expired) {
            try {
                if (ex.getStatus() != StatusExercice.IN_PROGRESS) continue;

                ex.setStatus(StatusExercice.COMPLETED);
                exerciceRepository.save(ex);
                exerciceService.onExerciceEnded(ex);
                log.info("Exercice terminé automatiquement → {} (id: {})", ex.getName(), ex.getId());
            } catch (Exception e) {
                log.error("Échec clôture exercice {} : {}", ex.getId(), e.getMessage(), e);
            }
        }

        // Étape 2 : DÉMARRER les exercices dus (seulement si aucun en cours)
        if (exerciceRepository.findFirstByStatus(StatusExercice.IN_PROGRESS).isEmpty()) {
            List<Exercice> toStart = exerciceRepository.findByStatusAndStartDateLessThanEqual(
                    StatusExercice.PLANNED, now);

            for (Exercice ex : toStart) {
                try {
                    if (ex.getStatus() != StatusExercice.PLANNED) continue;

                    ex.setStatus(StatusExercice.IN_PROGRESS);
                    exerciceRepository.save(ex);
                    exerciceService.onExerciceStarted(ex);
                    log.info("Exercice démarré automatiquement → {} (id: {})", ex.getName(), ex.getId());
                } catch (Exception e) {
                    log.error("Échec démarrage exercice {} : {}", ex.getId(), e.getMessage(), e);
                }
            }
        } else {
            log.warn("Démarrage exercice bloqué : un exercice est déjà IN_PROGRESS");
        }
    }*/

    /**
     * Vérifie toutes les heures si des sessions doivent être clôturées automatiquement
     * après 24h d'activité
     */
//    @Scheduled(cron = "0 0 * * * ?")  // Toutes les heures
//    // @Scheduled(fixedDelay = 3600000)  // Alternative : toutes les heures aussi
//    public void checkAndCloseSessions() {
//        log.info("Début de la vérification des sessions à clôturer automatiquement");
//
//        try {
//            sessionService.autoCloseSessionsAfter24h();
//            log.info("Vérification des sessions terminée");
//        } catch (Exception e) {
//            log.error("Erreur lors de la vérification des sessions à clôturer", e);
//        }
//    }
}