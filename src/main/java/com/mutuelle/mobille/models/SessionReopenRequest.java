package com.mutuelle.mobille.models;

import com.mutuelle.mobille.enums.StatusReopenRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_reopen_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionReopenRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "requested_by_email", nullable = false)
    private String requestedByEmail;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusReopenRequest status = StatusReopenRequest.PENDING;

    @Column(name = "president_approved")
    private Boolean presidentApproved;

    @Column(name = "president_approved_at")
    private LocalDateTime presidentApprovedAt;

    @Column(name = "president_email")
    private String presidentEmail;

    @Column(name = "tresorier_approved")
    private Boolean tresorierApproved;

    @Column(name = "tresorier_approved_at")
    private LocalDateTime tresorierApprovedAt;

    @Column(name = "tresorier_email")
    private String tresorierEmail;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
