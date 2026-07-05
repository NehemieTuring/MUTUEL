package com.mutuelle.mobille.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMemberDTO {
    private Long id;
    private Long memberId;
    private BigDecimal savingAmount;
    private BigDecimal unpaidRegistrationAmount;
    private BigDecimal baseRegistrationAmount;
    private BigDecimal solidarityAmount;
    private BigDecimal borrowAmount;
    private BigDecimal unpaidRenfoulement;
    private LocalDateTime lastInterestDate;
    private BigDecimal unpaidSolidarityAmount ;
    private BigDecimal maxBorrow;
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}