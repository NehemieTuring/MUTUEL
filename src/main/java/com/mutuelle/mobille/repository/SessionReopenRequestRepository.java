package com.mutuelle.mobille.repository;

import com.mutuelle.mobille.enums.StatusReopenRequest;
import com.mutuelle.mobille.models.SessionReopenRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionReopenRequestRepository extends JpaRepository<SessionReopenRequest, Long> {

    Optional<SessionReopenRequest> findBySessionIdAndStatus(Long sessionId, StatusReopenRequest status);

    boolean existsBySessionIdAndStatus(Long sessionId, StatusReopenRequest status);

    List<SessionReopenRequest> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
