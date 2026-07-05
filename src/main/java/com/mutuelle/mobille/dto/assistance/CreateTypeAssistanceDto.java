package com.mutuelle.mobille.dto.assistance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTypeAssistanceDto(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "Le montant maximum est obligatoire")
        @PositiveOrZero(message = "Le montant maximum doit être positif ou zéro")
        BigDecimal amount,

        Boolean active // optionnel, défaut true
) {}