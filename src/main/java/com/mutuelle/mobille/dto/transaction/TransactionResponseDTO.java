package com.mutuelle.mobille.dto.transaction;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionResponseDTO(
        Long id,
        BigDecimal amount,
        TransactionType transactionType,
        TransactionDirection transactionDirection,
        Long accountMemberId,
        String memberFullName,
        Long sessionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TransactionResponseDTO> children
) {}