package com.mutuelle.mobille.dto.member;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MemberRegistrationSetupDTO(
        @NotNull(message = "La date de première inscription est obligatoire")
        LocalDate firstRegistrationDate,

        @NotNull(message = "Le montant historique payé est obligatoire")
        @DecimalMin(value = "0.0", inclusive = true, message = "Le montant ne peut pas être négatif")
        @Digits(integer = 12, fraction = 2)
        BigDecimal historicalRegistrationPaid
) {}
