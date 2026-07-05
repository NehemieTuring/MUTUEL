package com.mutuelle.mobille.service.schedules;

import com.mutuelle.mobille.service.EmpruntService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class FinancialSchedules {

    private final EmpruntService empruntService;

    /**
     * Vérifie et applique les intérêts trimestriels sur les emprunts en cours
     * Exécuté tous les jours à 6h00 du matin
     */
//    @Scheduled(cron = "0 0 6 * * ?")
//    public void processTrimestrialInterests() {
//        log.info("Début du traitement quotidien des intérêts trimestriels");
//
//        try {
//            empruntService.calculerEtRedistribuerInteretsPenalites();
//            log.info("Traitement des intérêts trimestriels terminé avec succès");
//        } catch (Exception e) {
//            log.error("Erreur lors du calcul / redistribution des intérêts trimestriels", e);
//            // Option : alerte admin (email, Slack, Sentry, etc.)
//            // alertService.sendCriticalError("Échec intérêts trimestriels", e);
//        }
//    }

}