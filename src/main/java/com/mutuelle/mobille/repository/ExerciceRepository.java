package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.StatusExercice;
import com.mutuelle.mobille.models.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    // Trouver l'exercice dont la période contient la date donnée
    @Query("SELECT e FROM Exercice e WHERE e.startDate <= :now AND (e.endDate IS NULL OR e.endDate >= :now)")
    Optional<Exercice> findCurrentExercice(LocalDateTime now);

    // Vérifier s'il existe un exercice qui chevauche les dates données (excluant l'id fourni pour les updates)
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDateTime end, LocalDateTime start, Long excludeId);

    boolean existsByStartDateBetweenOrEndDateBetween(
            LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2);

    List<Exercice> findByStatusAndStartDateLessThanEqual(StatusExercice status, LocalDateTime date);

    List<Exercice> findByStatusAndEndDateLessThan(StatusExercice status, LocalDateTime date);

    Optional<Exercice> findFirstByStatus(StatusExercice status);

    // Exercice actif (status = ACTIVE + période valide)
    @Query("""
        SELECT e FROM Exercice e
        WHERE e.status = :status
          AND e.startDate <= :now
          AND (e.endDate IS NULL OR e.endDate >= :now)
    """)
    Optional<Exercice> findCurrentActiveExercice(
            @Param("status") StatusExercice status,
            @Param("now") LocalDateTime now
    );

    // Vérifie chevauchement de dates
    @Query("""
    SELECT COUNT(e) > 0 FROM Exercice e
    WHERE e.startDate <= :proposedEnd
      AND :proposedStart <= COALESCE(e.endDate, :proposedEnd)
      AND (:excludeId IS NULL OR e.id <> :excludeId)
    """)
    boolean existsOverlapping(
            @Param("proposedStart") LocalDateTime proposedStart,
            @Param("proposedEnd")   LocalDateTime proposedEnd,
            @Param("excludeId")     Long excludeId
    );
}