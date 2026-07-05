package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.bilan.MemberSessionBilan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberSessionBilanRepository extends JpaRepository<MemberSessionBilan, Long> {

    Optional<MemberSessionBilan> findByMemberIdAndSessionId(Long memberId, Long sessionId);

    boolean existsByMemberIdAndSessionId(Long memberId, Long sessionId);

    List<MemberSessionBilan> findByMemberId(Long memberId);

    List<MemberSessionBilan> findBySessionId(Long sessionId);

    @Query("SELECT b FROM MemberSessionBilan b WHERE b.session.exercice.id = :exerciceId")
    List<MemberSessionBilan> findBySessionExerciceId(@Param("exerciceId") Long exerciceId);

    @Query("SELECT b FROM MemberSessionBilan b WHERE b.member.id = :memberId AND b.session.exercice.id = :exerciceId")
    List<MemberSessionBilan> findByMemberIdAndExerciceId(@Param("memberId") Long memberId, @Param("exerciceId") Long exerciceId);
}
