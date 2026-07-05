package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.bilan.MemberExerciceBilan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberExerciceBilanRepository extends JpaRepository<MemberExerciceBilan, Long> {

    Optional<MemberExerciceBilan> findByMemberIdAndExerciceId(Long memberId, Long exerciceId);

    boolean existsByMemberIdAndExerciceId(Long memberId, Long exerciceId);

    List<MemberExerciceBilan> findByMemberId(Long memberId);

    List<MemberExerciceBilan> findByExerciceId(Long exerciceId);
}
