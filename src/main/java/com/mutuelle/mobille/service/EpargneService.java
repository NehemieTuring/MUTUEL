package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.mapper.TransactionMapper;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EpargneService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final SessionService sessionService;

    @Transactional
    public TransactionResponseDTO processEpargne(Long memberId, BigDecimal amount, TransactionDirection direction) {

        AccountMember memberAccount = accountService.getMemberAccount(memberId);

        Optional<Session> currentSessionOpt = sessionService.findCurrentSession();
        if (currentSessionOpt.isEmpty()) {
            throw new IllegalStateException("Pas de Session en cours");
        }
        Session currentSession = currentSessionOpt.get();

        if (direction == TransactionDirection.CREDIT) {
            accountService.addSaving(memberId, amount);
        } else if (direction == TransactionDirection.DEBIT) {
            accountService.withdrawSaving(memberId, amount);
        } else {
            throw new IllegalArgumentException("TransactionDirection invalide");
        }

        Transaction transaction = Transaction.builder()
                .accountMember(memberAccount)
                .session(currentSession)
                .amount(amount)
                .transactionType(TransactionType.EPARGNE)
                .transactionDirection(direction)
                .build();

        return TransactionMapper.toResponseDTO(transactionRepository.save(transaction));
    }



    public List<Transaction> getAllEpargneTransactions() {
        return transactionRepository.findByTransactionType(TransactionType.EPARGNE);
    }
//
//    public List<Transaction> getEpargneByMember(Long memberId) {
//        return transactionRepository.findByTransactionTypeAndAccountMemberId(TransactionType.EPARGNE, memberId);
//    }

    public List<Transaction> getEpargneByDirection(TransactionDirection direction) {
        return transactionRepository.findByTransactionTypeAndTransactionDirection(
                TransactionType.EPARGNE, direction);
    }

//    public List<Transaction> getEpargneByMemberAndDirection(Long memberId, TransactionDirection direction) {
//        return transactionRepository.findByAccountMemberIdAndTransactionTypeAndTransactionDirection(
//                memberId, TransactionType.EPARGNE, direction);
//    }

}
