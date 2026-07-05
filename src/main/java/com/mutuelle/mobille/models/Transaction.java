package com.mutuelle.mobille.models;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.account.AccountMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "description" )
    private String description = "";

    @Column(name = "transaction_direction", nullable = false)
    private TransactionDirection transactionDirection;
    
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    // === Relation avec accounts_member : ManyToOne ===
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "accounts_member_id", nullable = true)
    private AccountMember accountMember;

    // === Relation avec Session : ManyToOne ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    // transaction parente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transaction_id", nullable = true)
    private Transaction parentTransaction;

    @OneToMany(mappedBy = "parentTransaction", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> children = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}