package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.contribution.ContributionPaymentRequestDto;
import com.mutuelle.mobille.dto.contribution.ContributionPaymentResponseDto;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final AccountService accountService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;
    private final MemberService memberService;

    @Transactional
    public ContributionPaymentResponseDto processContributionPayment(ContributionPaymentRequestDto request) {

        TransactionType type = request.contributionType();
        BigDecimal amount = request.amount();
        Long memberId = request.memberId();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant payé doit être strictement positif");
        }

        AccountMember memberAccount = accountService.getMemberAccount(memberId);

        Session currentSession = sessionService.findCurrentSession()
                .orElseThrow(() -> new IllegalStateException("Aucune session en cours trouvée"));

        BigDecimal unpaidBefore = getUnpaidAmount(memberAccount, type);
        String contributionName = getContributionName(type);

        if (unpaidBefore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Le membre n'a plus de " + contributionName + " impayés");
        }

        if (amount.compareTo(unpaidBefore) > 0) {
            throw new IllegalArgumentException(
                    String.format("Le montant payé (%s) dépasse le montant dû (%s) pour le %s",
                            amount, unpaidBefore, contributionName)
            );
        }

        AccountService.RenfoulementSplit split = null;
        switch (type) {
            case INSCRIPTION -> accountService.payFeeInscriptionAmount(memberId, amount);
            case RENFOULEMENT -> split = accountService.payRenfoulementAmount(memberId, amount);
            default -> throw new IllegalArgumentException("Type non supporté");
        }

        // Mettre à jour le statut du membre après modification des dettes
        memberService.updateMemberStatus(memberAccount);

        BigDecimal remaining = unpaidBefore.subtract(amount);

        // Transaction parente
        Transaction transaction = Transaction.builder()
                .accountMember(memberAccount)
                .session(currentSession)
                .amount(amount)
                .transactionType(type)
                .transactionDirection(TransactionDirection.CREDIT)
                .description("Paiement " + contributionName)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Transactions enfants de ventilation (uniquement pour RENFOULEMENT)
        if (split != null) {
            if (split.partInscription().compareTo(BigDecimal.ZERO) > 0) {
                transactionRepository.save(Transaction.builder()
                        .accountMember(memberAccount)
                        .session(currentSession)
                        .amount(split.partInscription())
                        .transactionType(TransactionType.RENFOULEMENT_INSCRIPTION)
                        .transactionDirection(TransactionDirection.CREDIT)
                        .description("Ventilation renfoulement  Caisse inscription")
                        .parentTransaction(savedTransaction)
                        .build());
            }
            if (split.partSolidarite().compareTo(BigDecimal.ZERO) > 0) {
                transactionRepository.save(Transaction.builder()
                        .accountMember(memberAccount)
                        .session(currentSession)
                        .amount(split.partSolidarite())
                        .transactionType(TransactionType.RENFOULEMENT_SOLIDARITE)
                        .transactionDirection(TransactionDirection.CREDIT)
                        .description("Ventilation renfoulement  Caisse solidarité")
                        .parentTransaction(savedTransaction)
                        .build());
            }
        }

        return new ContributionPaymentResponseDto(
                savedTransaction.getId(),
                memberAccount.getMember().getId(),
                memberAccount.getMember().getFirstname() + " " + memberAccount.getMember().getLastname(),
                amount,
                type,
                TransactionDirection.CREDIT,
                remaining,
                LocalDateTime.now(),
                "Paiement de " + amount + " FCFA enregistré pour le " + contributionName +
                        ". Reste à payer : " + remaining + " FCFA"
        );
    }
    
    private BigDecimal getUnpaidAmount(AccountMember account, TransactionType type) {
        return switch (type) {
            case INSCRIPTION -> account.getUnpaidRegistrationAmount();
            case RENFOULEMENT -> account.getUnpaidRenfoulement();
            default -> throw new IllegalArgumentException("Type non supporté : " + type);
        };
    }

    private String getContributionName(TransactionType type) {
        return switch (type) {
            case INSCRIPTION -> "frais d'inscription";
            case RENFOULEMENT -> "renfoulement";
            default -> "contribution";
        };
    }
}