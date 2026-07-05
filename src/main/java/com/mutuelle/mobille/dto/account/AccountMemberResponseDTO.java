package com.mutuelle.mobille.dto.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountMemberResponseDTO {

    private Long id;
    private Long memberId;               // ID du membre associé
    private String memberFullName;       // Prénom + Nom (pour affichage rapide)
    private BigDecimal unpaidRegistrationAmount;
    private BigDecimal solidarityAmount;
    private BigDecimal savingAmount;
    private BigDecimal borrowAmount;
    private BigDecimal unpaidRenfoulement;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}