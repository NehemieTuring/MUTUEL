package com.mutuelle.mobille.dto.assistance;

import com.mutuelle.mobille.enums.AssistanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAssistanceStatusDto(
    @NotNull(message = "Le statut est obligatoire")
    AssistanceStatus status,

    @Size(max = 500, message = "La raison ne peut pas dépasser 500 caractères")
    String rejectReason
) {}
