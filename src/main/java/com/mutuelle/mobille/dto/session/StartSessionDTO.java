package com.mutuelle.mobille.dto.session;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionDTO {
    @NotNull(message = "L'ID de la session est obligatoire")
    private Long sessionId;
}