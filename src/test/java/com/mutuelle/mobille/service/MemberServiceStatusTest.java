package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.MemberStatus;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.account.AccountMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceStatusTest {

    @Mock
    private MutuelleConfigService mutuelleConfigService;

    @Mock
    private MemberComplianceService memberComplianceService;

    @InjectMocks
    private MemberService memberService;

    @Test
    void calculateMemberStatus_ignoresBorrowAmount() {
        MutuelleConfig config = MutuelleConfig.builder()
                .insolvencyThreshold(new BigDecimal("250000"))
                .build();
        when(mutuelleConfigService.getCurrentConfig()).thenReturn(config);

        AccountMember account = AccountMember.builder()
                .unpaidRegistrationAmount(BigDecimal.ZERO)
                .unpaidSolidarityAmount(BigDecimal.ZERO)
                .unpaidRenfoulement(BigDecimal.ZERO)
                .borrowAmount(new BigDecimal("500000"))
                .build();

        assertEquals(MemberStatus.ACTIF, memberService.calculateMemberStatus(account));
    }

    @Test
    void calculateMemberStatus_nonAJourWhenSolidarityDebt() {
        MutuelleConfig config = MutuelleConfig.builder()
                .insolvencyThreshold(new BigDecimal("250000"))
                .build();
        when(mutuelleConfigService.getCurrentConfig()).thenReturn(config);

        AccountMember account = AccountMember.builder()
                .unpaidRegistrationAmount(BigDecimal.ZERO)
                .unpaidSolidarityAmount(new BigDecimal("5000"))
                .unpaidRenfoulement(BigDecimal.ZERO)
                .borrowAmount(BigDecimal.ZERO)
                .build();

        assertEquals(MemberStatus.NON_A_JOUR, memberService.calculateMemberStatus(account));
    }
}
