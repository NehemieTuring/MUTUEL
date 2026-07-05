package com.mutuelle.mobille.mapper;


import com.mutuelle.mobille.dto.assistance.AssistanceResponseDto;
import com.mutuelle.mobille.models.Assistance;
import org.springframework.stereotype.Component;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssistanceMapper {

    public AssistanceResponseDto toResponseDto(Assistance assistance) {
        if (assistance == null) {
            return null;
        }

        return AssistanceResponseDto.builder()
                .id(assistance.getId())
                .description(assistance.getDescription())

                .typeAssistanceId(assistance.getTypeAssistance().getId())
                .typeAssistanceName(assistance.getTypeAssistance().getName())
                .typeAssistanceAmount(assistance.getTypeAssistance().getAmount())

                .memberId(assistance.getMember().getId())
                .memberFullName(assistance.getMember().getFirstname()+ " " + assistance.getMember().getLastname())

                .sessionId(assistance.getSession().getId())
                .sessionName(assistance.getSession().getName())

                .transaction(TransactionMapper.toResponseDTO(assistance.getTransaction()))

                .amountMove(assistance.getAmountMove())
                .createdAt(assistance.getCreatedAt())
                .updatedAt(assistance.getUpdatedAt())
                .build();
    }

    // Si tu veux mapper une liste
    public List<AssistanceResponseDto> toResponseDtoList(List<Assistance> assistances) {
        if (assistances == null) {
            return Collections.emptyList();
        }
        return assistances.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}