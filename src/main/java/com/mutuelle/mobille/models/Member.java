package com.mutuelle.mobille.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String phone;

    @Column(name = "avatar", nullable = true)
    private String avatar;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "pin", nullable = false, length = 4)
    private String pin = "2025";

    // Relation OneToOne obligatoire et bidirectionnelle avec Account
    @OneToOne(mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = false)
    @JsonBackReference
    private AccountMember accountMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20) DEFAULT 'ACTIF' NOT NULL")
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIF;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pin == null) {
            pin = "2025";
        }
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}