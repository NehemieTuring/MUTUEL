package com.mutuelle.mobille.dto.assistance;

import jakarta.validation.constraints.NotNull;

public record CreateAssistanceDto(
        @NotNull(message = "L'ID du type d'assistance est obligatoire")
        Long typeAssistanceId,

        @NotNull(message = "L'ID de la session est obligatoire")
        Long sessionId,

        @NotNull(message = "L'ID du membre est obligatoire")
        Long memberId,

        String description
) {}