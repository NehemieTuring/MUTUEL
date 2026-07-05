package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.exercice.ExerciceHistoryDto;
import com.mutuelle.mobille.models.ExerciceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExerciceHistoryMapper {

    public static ExerciceHistoryDto toDto(ExerciceHistory entity) {
        if (entity == null) {
            return null;
        }

        return ExerciceHistoryDto.builder()
                .id(entity.getId())
                .exerciceId(entity.getExercice().getId())
                .exerciceName(entity.getExercice().getName())
                .exerciceStartDate(entity.getExercice().getStartDate())
                .exerciceEndDate(entity.getExercice().getEndDate())
                .exerciceStatus(entity.getExercice().getStatus())
                .totalAssistanceAmount(entity.getTotalAssistanceAmount())
                .totalAssistanceCount(entity.getTotalAssistanceCount())
                .totalAgapeAmount(entity.getTotalAgapeAmount())
                .mutuelleCash(entity.getMutuelleCash())
                .mutuellesSavingAmount(entity.getMutuellesSavingAmount())
                .mutuelleBorrowAmount(entity.getMutuelleBorrowAmount())
                .totalTransactions(entity.getTotalTransactions())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}