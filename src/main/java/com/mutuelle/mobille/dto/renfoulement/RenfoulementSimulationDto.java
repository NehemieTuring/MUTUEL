package com.mutuelle.mobille.dto.renfoulement;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RenfoulementSimulationDto {
    private String exerciceName;
    private BigDecimal estimatedTotalToDistributeAmount;
    private int estimatedBaseMembersCount;
    private BigDecimal estimatedUnitAmount;
    private int estimatedDistributedMembersCount;
    private BigDecimal estimatedExpectedTotalAmount;
    private Boolean isPossible;
    private String message;
}