package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // ───────────────────────────────────────────────
    //              Recherche de la session courante
    // ───────────────────────────────────────────────

    /**
     * Recherche la session qui devrait être en cours à une date donnée
     * (startDate ≤ now ET (endDate IS NULL OU endDate ≥ now))
     * et qui est au statut IN_PROGRESS ou PLANNED (mais on filtrera le statut dans le service si besoin)
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.startDate <= :now
          AND (s.endDate IS NULL OR s.endDate >= :now)
          AND s.status IN :statuses
        ORDER BY s.startDate DESC
        LIMIT 1
    """)
    Optional<Session> findActiveSessionAt(
            @Param("now") LocalDateTime now,
            @Param("statuses") List<StatusSession> statuses);

    // Variante la plus courante (IN_PROGRESS uniquement)
    default Optional<Session> findCurrentInProgress(LocalDateTime now) {
        return findActiveSessionAt(now, List.of(StatusSession.IN_PROGRESS));
    }

    // ───────────────────────────────────────────────
    //              Pour le scheduler (tâches planifiées)
    // ───────────────────────────────────────────────

    /**
     * Sessions qui devraient démarrer maintenant ou dans le passé
     * (PLANNED + startDate ≤ now)
     /
    @Query("""
        SELECT s FROM Session s
        WHERE s.status = 'PLANNED'
        ORDER BY s.startDate ASC
    """)
    List<Session> findSessionsDueToStart(@Param("now") LocalDateTime now);*/

    /**
     * Sessions planifiées (sans condition de date)
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.status = 'PLANNED'
        ORDER BY s.createdAt ASC
    """)
    List<Session> findAllPlannedSessions();

    /**
     * Sessions qui sont en cours mais dont la date de fin est dépassée
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.status = 'IN_PROGRESS'
          AND s.endDate IS NOT NULL
          AND s.endDate < :now
        ORDER BY s.endDate ASC
    """)
    List<Session> findExpiredInProgressSessions(@Param("now") LocalDateTime now);

    // ───────────────────────────────────────────────
    //              Vérifications d'intégrité
    // ───────────────────────────────────────────────

    /**
     * Vérifie s'il existe une session qui chevauche l'intervalle [start, end]
     * (end peut être LocalDateTime.MAX si pas de fin)
     */
//    @Query("""
//        SELECT COUNT(s) > 0 FROM Session s
//        WHERE s.id != :excludeId
//          AND s.startDate < :candidateEnd
//          AND (s.endDate IS NULL OR s.endDate > :candidateStart)
//    """)
//    boolean existsOverlapping(
//            @Param("candidateStart") LocalDateTime candidateStart,
//            @Param("candidateEnd") LocalDateTime candidateEnd,
//            @Param("excludeId") Long excludeId);

    @Query("""
    SELECT COUNT(s) > 0 FROM Session s
    WHERE s.startDate <= :proposedEnd
      AND :proposedStart <= COALESCE(s.endDate, :proposedEnd)
      AND (:excludeId IS NULL OR s.id <> :excludeId)
    """)
    boolean existsOverlapping(
            @Param("proposedStart") LocalDateTime proposedStart,
            @Param("proposedEnd")   LocalDateTime proposedEnd,
            @Param("excludeId")     Long excludeId
    );


    // Variante plus permissive (pour debug ou cas spéciaux)
    default boolean existsOverlapping(Session session, Long excludeId) {
        LocalDateTime end = session.getEndDate() != null ? session.getEndDate() : LocalDateTime.MAX;
        return existsOverlapping(session.getStartDate(), end, excludeId);
    }

    /**
     * Vérifie s'il existe déjà une session IN_PROGRESS (devrait être au maximum 1)
     */
    Optional<Session> findFirstByStatus(StatusSession status);
    List<Session> findByStatusAndStartDateLessThanEqual(StatusSession status, LocalDateTime date);
    List<Session> findByStatusAndEndDateLessThan(StatusSession status, LocalDateTime date);
    List<Session> findByStatus(StatusSession status);

    // ───────────────────────────────────────────────
    //              Requêtes utiles existantes / conservées
    // ───────────────────────────────────────────────

    List<Session> findByExerciceId(Long exerciceId);

    List<Session> findByExerciceIdAndStatus(Long exerciceId, StatusSession status);

    // Pour lister les sessions planifiées ou terminées d'un exercice
    List<Session> findByExerciceIdAndStatusIn(Long exerciceId, List<StatusSession> statuses);

    @Query("""
        SELECT COUNT(s) > 0 FROM Session s
        WHERE s.status = 'IN_PROGRESS'
          AND (:excludeId IS NULL OR s.id != :excludeId)
    """)
    boolean existsOtherInProgress(@Param("excludeId") Long excludeId);

    //Recherche des sessions creees durant un intervale de temps
    List<Session> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    // Nombre de sessions fermées (COMPLETED) dont la date de début est postérieure à la session d'emprunt
    @Query("SELECT COUNT(s) FROM Session s WHERE s.status = 'COMPLETED' AND s.startDate > :afterDate")
    long countCompletedSessionsAfter(@Param("afterDate") LocalDateTime afterDate);


    // Pour retrouver une session encore ouverte 24h apres son demarrage
    @Query("""
        SELECT s FROM Session s
        WHERE s.status = 'IN_PROGRESS'
          AND s.startDate <= :twentyFourHoursAgo
    """)
    List<Session> findInProgressSessionsOlderThan(@Param("twentyFourHoursAgo") LocalDateTime twentyFourHoursAgo);
}