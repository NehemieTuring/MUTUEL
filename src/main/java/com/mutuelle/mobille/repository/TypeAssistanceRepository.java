package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.TypeAssistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeAssistanceRepository extends JpaRepository<TypeAssistance, Long> {

    Optional<TypeAssistance> findByNameIgnoreCase(String name);

    // Optionnel : pour vérifier l'unicité du nom (utile dans le service si besoin)
    boolean existsByNameIgnoreCase(String name);
}