package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.MemberStatus;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpargneServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private SessionService sessionService;

    @InjectMocks
    private EpargneService epargneService;

    @Test
    void withdrawBlockedWhenMemberHasActiveLoan() {
        Member member = new Member();
        member.setStatus(MemberStatus.ACTIF);

        AccountMember account = AccountMember.builder()
                .member(member)
                .borrowAmount(new BigDecimal("100000"))
                .build();

        when(accountService.getMemberAccount(1L)).thenReturn(account);
        when(sessionService.findCurrentSession()).thenReturn(Optional.of(new Session()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                epargneService.processEpargne(1L, new BigDecimal("5000"), TransactionDirection.DEBIT));

        assertTrue(ex.getMessage().contains("prêt d'emprunt en cours"));
        verify(accountService, never()).withdrawSaving(anyLong(), any());
    }

    @Test
    void withdrawAllowedWhenNoActiveLoan() {
        Member member = new Member();
        member.setStatus(MemberStatus.ACTIF);

        AccountMember account = AccountMember.builder()
                .member(member)
                .borrowAmount(BigDecimal.ZERO)
                .build();

        when(accountService.getMemberAccount(1L)).thenReturn(account);
        when(sessionService.findCurrentSession()).thenReturn(Optional.of(new Session()));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                epargneService.processEpargne(1L, new BigDecimal("5000"), TransactionDirection.DEBIT));

        verify(accountService).withdrawSaving(1L, new BigDecimal("5000"));
    }
}
