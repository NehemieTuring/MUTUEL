package com.mutuelle.mobille.dto.contribution;

import com.mutuelle.mobille.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;


public record ContributionPaymentRequestDto(

        @NotNull(message = "L'ID du membre est obligatoire")
        Long memberId,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        BigDecimal amount,

        @NotNull(message = "Le type de contribution est obligatoire")
        TransactionType contributionType

) {

    @AssertTrue(message = "Le type de contribution doit être  INSCRIPTION ou RENFOULEMENT")
    private boolean isValidContributionType() {
        return  contributionType == TransactionType.INSCRIPTION ||
                contributionType == TransactionType.RENFOULEMENT;
    }
}