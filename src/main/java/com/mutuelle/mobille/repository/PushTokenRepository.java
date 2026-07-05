package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.models.PushToken;
import com.mutuelle.mobille.models.auth.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    List<PushToken> findByUser(AuthUser user);

    Optional<PushToken> findByToken(String token);

    void deleteByToken(String token);
}