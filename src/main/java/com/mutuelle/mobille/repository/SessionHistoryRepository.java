package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.SessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {
    boolean existsBySessionId(Long sessionId);
    @Query("""
        SELECT sh FROM SessionHistory sh
        JOIN FETCH sh.session s
        JOIN FETCH s.exercice e
        WHERE e.id = :exerciceId
        ORDER BY s.startDate ASC
    """)
    List<SessionHistory> findAllByExerciceId(
            @Param("exerciceId") Long exerciceId
    );

    // Pour récupérer par session
    Optional<SessionHistory> findBySessionId(Long sessionId);
}