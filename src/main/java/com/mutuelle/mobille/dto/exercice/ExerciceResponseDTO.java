package com.mutuelle.mobille.dto.exercice;

import com.mutuelle.mobille.enums.StatusExercice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExerciceResponseDTO {

    private Long id;
    private String name;
    private LocalDateTime startDate;
    private StatusExercice status;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}