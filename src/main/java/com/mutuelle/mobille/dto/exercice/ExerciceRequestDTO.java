package com.mutuelle.mobille.dto.exercice;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExerciceRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String name;

   /* @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    private LocalDateTime endDate; // optionnelle (peut être null si exercice en cours)*/
}