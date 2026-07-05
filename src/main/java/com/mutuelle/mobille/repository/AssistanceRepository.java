package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.Assistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Long> {

    // Utile pour récupérer les demandes d'un membre
    List<Assistance> findByMemberId(Long memberId);

    // Ou par session
    List<Assistance> findBySessionId(Long sessionId);

    // Par membre et session (si besoin)
    List<Assistance> findByMemberIdAndSessionId(Long memberId, Long sessionId);

    //  Nombre d'assistances pour un membre
    Long countByMemberId(Long memberId);

    //   Nombre d'assistances pour une session
    Long countBySessionId(Long sessionId);

    @Query("""
    SELECT a FROM Assistance a
    WHERE (:typeAssistanceId IS NULL OR a.typeAssistance.id = :typeAssistanceId)
      AND (:memberId IS NULL OR a.member.id = :memberId)
      AND (:sessionId IS NULL OR a.session.id = :sessionId)
      AND (cast(:fromDate as timestamp) IS NULL OR a.createdAt >= :fromDate)
      AND (cast(:toDate as timestamp)   IS NULL OR a.createdAt <= :toDate)
    ORDER BY a.createdAt DESC
    """)
    Page<Assistance> findAllFiltered(
            @Param("typeAssistanceId") Long typeAssistanceId,
            @Param("memberId") Long memberId,
            @Param("sessionId") Long sessionId,
            @Param("fromDate") LocalDateTime fromDate,   // garde LocalDateTime, même si null
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}