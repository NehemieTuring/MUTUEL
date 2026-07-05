package com.mutuelle.mobille.dto.contribution;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContributionPaymentResponseDto(
        Long transactionId,

        Long memberId,

        String memberFullName,

        BigDecimal amountPaid,

        TransactionType contributionType,

        TransactionDirection direction,

        BigDecimal remainingUnpaidAmount,
        LocalDateTime paymentDate,

        String message
) {}