package com.mutuelle.mobille.dto.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountMutuelleResponseDTO {

    private Long id;
    private BigDecimal savingAmount;
    private BigDecimal solidarityAmount;
    private BigDecimal borrowAmount;
    private BigDecimal unpaidRegistrationAmount;
    private BigDecimal unpaidRenfoulement;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}