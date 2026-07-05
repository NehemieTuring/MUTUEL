package com.mutuelle.mobille.dto.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigMutuelleRequestDto {

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Digits(integer = 12, fraction = 2, message = "Format du montant invalide (max 12 chiffres entiers, 2 décimales)")
    private BigDecimal registrationFeeAmount;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant de solidarité doit être positif")
    @Digits(integer = 12, fraction = 2, message = "Format du montant invalide (max 12 chiffres entiers, 2 décimales)")
    private BigDecimal solidarityFeeAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le taux ne peut pas être négatif")
    @Digits(integer = 3, fraction = 2, message = "Format du taux invalide (ex: 12.50 → max 3 chiffres entiers)")
    private BigDecimal loanInterestRatePercent;
    @DecimalMin(value = "0.0", inclusive = true, message = "La pénalité fixe ne peut pas être négative")
    @Digits(integer = 12, fraction = 2, message = "Format invalide")
    private BigDecimal loanPenaltyFixedAmount;

    @Min(value = 1, message = "Le seuil de sessions doit être d'au moins 1")
    private Integer loanPenaltySessionThreshold;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le seuil d'insolvabilité doit être positif")
    @Digits(integer = 12, fraction = 2, message = "Format du seuil invalide")
    private BigDecimal insolvencyThreshold;
}