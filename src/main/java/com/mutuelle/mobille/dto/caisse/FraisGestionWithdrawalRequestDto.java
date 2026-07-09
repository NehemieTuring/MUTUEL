package com.mutuelle.mobille.dto.caisse;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Retrait de frais de gestion sur la caisse inscription")
public record FraisGestionWithdrawalRequestDto(
        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être strictement positif")
        BigDecimal amount,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 3, max = 500, message = "La description doit contenir entre 3 et 500 caractères")
        String description
) {}
