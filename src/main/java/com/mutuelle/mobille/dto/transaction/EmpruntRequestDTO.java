package com.mutuelle.mobille.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO utilisé pour effectuer un emprunt
 */
@Data
public class EmpruntRequestDTO {
    /**
     * Identifiant du membre qui souhaite emprunter
     */
    @NotNull(message = "L'identifiant du membre est obligatoire")
    private Long memberId;
    /**
     * Montant que le membre souhaite emprunter
   */
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être strictement positif")
    @DecimalMin(value = "25", message = "Le montant minimum d'un emprunt est 25")
    private BigDecimal amount;
}
