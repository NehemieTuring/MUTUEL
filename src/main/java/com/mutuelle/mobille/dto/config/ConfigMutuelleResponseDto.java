package com.mutuelle.mobille.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMutuelleResponseDto {

    private Long id;
    private BigDecimal registrationFeeAmount;
    private BigDecimal solidarityFeeAmount;
    private BigDecimal loanInterestRatePercent;
    private BigDecimal loanPenaltyFixedAmount;
    private Integer loanPenaltySessionThreshold;
    private BigDecimal insolvencyThreshold;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public ConfigMutuelleResponseDto(com.mutuelle.mobille.models.MutuelleConfig config) {
        this.id = config.getId();
        this.registrationFeeAmount = config.getRegistrationFeeAmount();
        this.solidarityFeeAmount = config.getSolidarityFeeAmount();
        this.loanInterestRatePercent = config.getLoanInterestRatePercent();
        this.loanPenaltyFixedAmount = config.getLoanPenaltyFixedAmount();
        this.loanPenaltySessionThreshold = config.getLoanPenaltySessionThreshold();
        this.insolvencyThreshold = config.getInsolvencyThreshold();
        this.updatedAt = config.getUpdatedAt();
        this.updatedBy = config.getUpdatedBy();
    }
}