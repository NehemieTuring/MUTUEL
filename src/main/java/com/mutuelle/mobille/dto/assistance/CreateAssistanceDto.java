package com.mutuelle.mobille.dto.assistance;

import jakarta.validation.constraints.NotNull;

public record CreateAssistanceDto(
        @NotNull(message = "L'ID du type d'assistance est obligatoire")
        Long typeAssistanceId,

        Long sessionId,    // optionnel — si null, utilise la session active
        Long memberId,     // optionnel — si null, utilise le membre connecté
        String description
) {}
