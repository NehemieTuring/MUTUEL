package com.mutuelle.mobille.dto.caisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraisGestionWithdrawalResponseDto(
        Long transactionId,
        BigDecimal amount,
        String description,
        Long sessionId,
        String sessionName,
        LocalDateTime createdAt,
        BigDecimal registrationBalanceAfter
) {}
