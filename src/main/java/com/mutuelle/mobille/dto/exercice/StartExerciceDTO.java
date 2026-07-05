package com.mutuelle.mobille.dto.exercice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartExerciceDTO {
    @NotNull(message = "L'ID de l'exercice est obligatoire")
    private Long exerciceId;
}