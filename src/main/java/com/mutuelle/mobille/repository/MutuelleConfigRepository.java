package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.MutuelleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MutuelleConfigRepository extends JpaRepository<MutuelleConfig, Long> {

    // On prend toujours la config la plus récente (au cas où tu veux garder l'historique plus tard)
    Optional<MutuelleConfig> findTopByOrderByUpdatedAtDesc();

    // Ou si tu veux une seule config active à tout moment :
    // Optional<MutuelleConfig> findById(Long id); déjà présent via JpaRepository
}