package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.auth.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    // Méthode CRUCIALE pour Spring Security / JWT
    Optional<AuthUser> findByEmail(String email);

    // Vérifier si un email existe déjà (inscription)
    boolean existsByEmail(String email);

    // Récupérer par type (MEMBER ou ADMIN)
    Optional<AuthUser> findByEmailAndRole(String email, Role role);

    Optional<AuthUser> findFirstByRole(Role role);

    // Pour les admins uniquement
    Optional<AuthUser> findByUserRefIdAndRole(Long userRefId, Role role);

    // Tous les utilisateurs d’un certain type
    long countByRole(Role role);

    List<AuthUser> findAllByRole(Role role);

    Optional<AuthUser> findByUserRefId(Long id);

}