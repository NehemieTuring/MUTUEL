package com.mutuelle.mobille.dto.member;

import com.mutuelle.mobille.enums.MemberStatus;
import com.mutuelle.mobille.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberResponseDTO(
        Long authId,
        Long id,
        String firstname,
        String lastname,
        String phone,
        String email,
        String avatar,
        Role role,
        boolean isActive,

        BigDecimal unpaidRegistrationAmount,
        BigDecimal baseRegistrationAmount,
        BigDecimal solidarityAmount,
        BigDecimal unpaidSolidarityAmount,
        BigDecimal borrowAmount,
        BigDecimal unpaidRenfoulement,
        BigDecimal savingAmount,
        Long idAccount,
        String pin,
        MemberStatus status,
        String statusLabel,

        LocalDate firstRegistrationDate,
        BigDecimal historicalRegistrationPaid,
        boolean registrationConfigured,

        boolean insolvable,
        int sessionsBeforeLoanBlock,
        boolean assistanceBlocked,
        int assistanceBlockedSessionsRemaining,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
