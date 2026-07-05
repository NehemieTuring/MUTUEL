package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.models.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Trouver un token valide (non révoqué et non expiré)
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    // Tous les tokens expirés (pour nettoyage périodique)
    Stream<RefreshToken> findAllByExpiryDateBeforeAndRevokedFalse(Instant now);

    // Révoquer tous les tokens d’un utilisateur (logout de tous les appareils)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.authUser = :authUser AND r.revoked = false")
    void revokeAllByAuthUser(AuthUser authUser);

    Optional<RefreshToken> findByToken(String token);

    // Méthode magique pour le logout global
    List<RefreshToken> findAllByAuthUserAndRevokedFalse(AuthUser authUser);

    // Optionnel : nettoyer les anciens
    void deleteByExpiryDateBeforeAndRevokedTrue(Instant date);

    // Nettoyage des tokens expirés
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    void deleteAllByExpiryDateBefore(Instant now);
}