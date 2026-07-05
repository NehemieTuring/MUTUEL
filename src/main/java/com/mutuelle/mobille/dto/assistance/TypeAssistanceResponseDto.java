package com.mutuelle.mobille.dto.assistance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TypeAssistanceResponseDto(
        Long id,
        String name,
        BigDecimal amount,
        String description
) {}