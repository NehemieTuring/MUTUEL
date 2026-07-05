package com.mutuelle.mobille.models.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts_mutuelle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountMutuelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant  ne peut pas être négatif")
    @Column(name = "saving_amount", precision = 12, scale = 2)
    private BigDecimal savingAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant  ne peut pas être négatif")
    @Column(name = "solidarity_amount", precision = 12, scale = 2)
    private BigDecimal solidarityAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant  ne peut pas être négatif")
    @Column(name = "registration_amount", precision = 12, scale = 2)
    private BigDecimal registrationAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant  ne peut pas être négatif")
    @Column(name = "borrow_amount", precision = 12, scale = 2)
    private BigDecimal borrowAmount = BigDecimal.ZERO;

//    @DecimalMin(value = "0.00", inclusive = true, message = "Le montant  ne peut pas être négatif")
//    @Column(name = "total_renfoulement", precision = 12, scale = 2)
//    private BigDecimal totalRenfoulement = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isActive = true;

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

    public BigDecimal getRegistrationAmount() {
        return registrationAmount == null ? BigDecimal.ZERO : registrationAmount;
    }
}