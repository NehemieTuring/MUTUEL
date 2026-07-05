package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.TransactionRepository;
import com.mutuelle.mobille.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InteretService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final MutuelleConfigService mutuelleConfigService;

    public BigDecimal calculerInteret(BigDecimal montantEmprunte) {
        if (montantEmprunte == null || montantEmprunte.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        BigDecimal taux = config.getLoanInterestRatePercent()
                .divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);

        return MoneyUtil.round(montantEmprunte.multiply(taux));
    }

    /**
     * Calcule le montant d'emprunt initial fictif qui aurait conduit au solde restant dû actuel (Valeur de l'interet restant),
     * en tenant compte de la formule : net_reçu = montant_emprunt - interet(montant_emprunt)
     * → montant_emprunt = solde_dû / (1 - taux)
     */
    public BigDecimal calculMontantEmpruntEquivalent(BigDecimal soldeRestant) {
        if (soldeRestant == null || soldeRestant.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        BigDecimal taux = config.getLoanInterestRatePercent()
                .divide(BigDecimal.valueOf(100), 8, BigDecimal.ROUND_HALF_UP);

        if (taux.compareTo(BigDecimal.ONE) >= 0 || taux.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Taux invalide ({}) - retour solde tel quel", taux);
            return soldeRestant;
        }

        BigDecimal diviseur = BigDecimal.ONE.subtract(taux);
        BigDecimal montantEquivalent = soldeRestant.divide(diviseur, 2, BigDecimal.ROUND_HALF_UP);

        return MoneyUtil.floorToNearest25(montantEquivalent);
    }

    public void redistribuerInteret(
                                    BigDecimal interetTotal,
                                    Transaction parentTransaction,
                                    Session session) {

        if (interetTotal == null || interetTotal.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Aucun intérêt à redistribuer");
            return;
        }

        List<AccountMember> beneficiaires = accountService.getAllMemberAccounts().stream()
                .filter(acc -> acc.getSavingAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (beneficiaires.isEmpty()) {
            log.info("Aucun bénéficiaire → tout en caisse");
            accountService.addToMutuelleCaisse(interetTotal);
            saveCaisseTransaction(interetTotal, "Intérêt - caisse (aucun épargnant)", session, parentTransaction);
            return;
        }

        BigDecimal totalEpargne = beneficiaires.stream()
                .map(AccountMember::getSavingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalEpargne.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Total épargne nulle → redistribution en caisse");
            accountService.addToMutuelleCaisse(interetTotal);
            return;
        }

        BigDecimal totalDistribue = BigDecimal.ZERO;

        for (AccountMember beneficiaire : beneficiaires) {
            BigDecimal partBrute = calculPartMembre(totalEpargne, beneficiaire.getSavingAmount(), interetTotal);

            // ARRONDISSAGE À L'INFÉRIEUR AU MULTIPLE DE 25
            BigDecimal interetPart = MoneyUtil.floorToNearest25(partBrute);

            // Crédit sur le compte
            beneficiaire.setSavingAmount(beneficiaire.getSavingAmount().add(interetPart));
            accountService.saveMemberAccount(beneficiaire);
            accountService.addToMutuelleCaisse(interetPart);

            // Transaction
            saveTransaction(beneficiaire, interetPart, "Redistribution d'intérêt", session, parentTransaction);

            totalDistribue = totalDistribue.add(interetPart);
        }

        // Reliquat → caisse
        BigDecimal reliquat = interetTotal.subtract(totalDistribue);
        if (reliquat.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Reliquat de {} FCFA envoyé à la caisse", reliquat);
            accountService.addToMutuelleCaisse(reliquat);
            saveCaisseTransaction(reliquat, "Reliquat d'intérêt (arrondi)", session, parentTransaction);
        }
    }

    private BigDecimal calculPartMembre(BigDecimal totalEpargne, BigDecimal epargneMembre, BigDecimal interetTotal) {
        if (totalEpargne.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return interetTotal.multiply(epargneMembre).divide(totalEpargne, 4, BigDecimal.ROUND_HALF_UP);
    }

    private void saveTransaction(AccountMember account, BigDecimal montant, String desc, Session session, Transaction parent) {
        Transaction tx = Transaction.builder()
                .accountMember(account)
                .amount(montant)
                .description(desc)
                .transactionType(TransactionType.INTERET)
                .transactionDirection(TransactionDirection.CREDIT)
                .session(session)
                .parentTransaction(parent)
                .build();
        transactionRepository.save(tx);
    }

    private void saveCaisseTransaction(BigDecimal montant, String desc, Session session, Transaction parent) {
        saveTransaction(null, montant, desc, session, parent);
    }
}