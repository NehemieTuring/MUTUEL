package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.caisse.FraisGestionWithdrawalRequestDto;
import com.mutuelle.mobille.dto.caisse.FraisGestionWithdrawalResponseDto;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraisGestionService {

    private final SessionService sessionService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public FraisGestionWithdrawalResponseDto withdraw(FraisGestionWithdrawalRequestDto request) {
        Session session = sessionService.findCurrentSession()
                .orElseThrow(() -> new IllegalStateException("Aucune session en cours. Ouvrez une session pour enregistrer un retrait."));

        if (session.getStatus() != StatusSession.IN_PROGRESS) {
            throw new IllegalStateException("Les retraits ne sont possibles que pendant une session ouverte.");
        }

        BigDecimal amount = request.amount();
        AccountMutuelle global = accountService.getMutuelleGlobalAccount();
        BigDecimal balance = global.getRegistrationAmount() != null
                ? global.getRegistrationAmount() : BigDecimal.ZERO;

        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException(String.format(
                    "Solde insuffisant dans la caisse inscription. Disponible : %s FCFA, demandé : %s FCFA",
                    balance, amount));
        }

        accountService.removeToRegistrationMutuelleCaisse(amount);

        Transaction tx = Transaction.builder()
                .session(session)
                .amount(amount)
                .transactionType(TransactionType.FRAIS_GESTION)
                .transactionDirection(TransactionDirection.DEBIT)
                .description(request.description().trim())
                .accountMember(null)
                .build();

        Transaction saved = transactionRepository.save(tx);
        global = accountService.getMutuelleGlobalAccount();

        return new FraisGestionWithdrawalResponseDto(
                saved.getId(),
                amount,
                saved.getDescription(),
                session.getId(),
                session.getName(),
                saved.getCreatedAt(),
                global.getRegistrationAmount()
        );
    }

    @Transactional(readOnly = true)
    public List<FraisGestionWithdrawalResponseDto> listByExercice(Long exerciceId) {
        return transactionRepository
                .findByExerciceIdAndTypeAndDirection(exerciceId, TransactionType.FRAIS_GESTION, TransactionDirection.DEBIT)
                .stream()
                .map(tx -> new FraisGestionWithdrawalResponseDto(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getDescription(),
                        tx.getSession() != null ? tx.getSession().getId() : null,
                        tx.getSession() != null ? tx.getSession().getName() : null,
                        tx.getCreatedAt(),
                        null
                ))
                .toList();
    }
}
