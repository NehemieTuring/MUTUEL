package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.ExerciceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciceHistoryRepository extends JpaRepository<ExerciceHistory, Long> {
    boolean existsByExerciceId(Long exerciceId);
    Optional<ExerciceHistory> findByExerciceId(Long exerciceId);
}