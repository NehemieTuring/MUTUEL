package com.mutuelle.mobille.dto.assistance;

import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.enums.AssistanceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistanceResponseDto {

    private Long id;
    private String description;

    private Long typeAssistanceId;
    private String typeAssistanceName;
    private BigDecimal typeAssistanceAmount;

    private Long memberId;
    private String memberFullName;

    private Long sessionId;
    private String sessionName;

    private TransactionResponseDTO transaction;

    private BigDecimal amountMove;

    private AssistanceStatus status;
    private String rejectReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
