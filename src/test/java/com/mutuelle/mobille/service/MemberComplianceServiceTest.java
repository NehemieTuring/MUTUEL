package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.MemberStatus;
import com.mutuelle.mobille.models.account.AccountMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MemberComplianceServiceTest {

    @Mock
    private MutuelleConfigService mutuelleConfigService;

    @InjectMocks
    private MemberComplianceService memberComplianceService;

    @Test
    void computeInsolvable_trueAfterThreeSessions() {
        AccountMember account = AccountMember.builder()
                .unpaidSolidarityAmount(new BigDecimal("10000"))
                .sessionsInNonAJour(3)
                .build();

        assertTrue(memberComplianceService.computeInsolvable(account));
        assertEquals(0, memberComplianceService.computeSessionsBeforeLoanBlock(account));
    }

    @Test
    void computeInsolvable_falseWhenNoDebt() {
        AccountMember account = AccountMember.builder()
                .sessionsInNonAJour(5)
                .build();

        assertFalse(memberComplianceService.computeInsolvable(account));
    }

    @Test
    void computeStatusLabel_pendingIsNonInscrit() {
        assertEquals("Non inscrit", memberComplianceService.computeStatusLabel(MemberStatus.PENDING));
        assertEquals("À jour", memberComplianceService.computeStatusLabel(MemberStatus.ACTIF));
        assertEquals("Non à jour", memberComplianceService.computeStatusLabel(MemberStatus.NON_A_JOUR));
    }
}
