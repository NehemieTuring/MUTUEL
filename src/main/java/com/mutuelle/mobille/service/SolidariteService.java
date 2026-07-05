package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.AccountMemberRepository;
import com.mutuelle.mobille.repository.AccountMutuelleRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.repository.SolidariteRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolidariteService {

        // Dépendances identiques au style de AccountService
        private final AccountMemberRepository memberRepo;
        private final AccountMutuelleRepository globalRepo;
        private final SessionRepository sessionRepo;
        private final SolidariteRepository solidariteRepo;
        private final AccountService accountService;
        private final MemberService memberService;

        /**
         * Paiement d'une cotisation de solidarité par un membre
         * - met à jour les comptes (membre + global)
         * - crée une transaction pour la traçabilité
         */
        @Transactional
        public void paySolidarity(Long memberId, BigDecimal amount, Long sessionId) {

                // ─────────────────────────────────────────────
                // VALIDATIONS MÉTIER
                // ─────────────────────────────────────────────
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Le montant de solidarité doit être positif");
                }

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                AccountMutuelle globalAccount = accountService.getMutuelleGlobalAccount();

                Session session = sessionRepo.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("Session introuvable"));

                //  La session doit être EN COURS
                if (session.getStatus() != StatusSession.IN_PROGRESS) {
                        throw new IllegalStateException(
                                String.format("Impossible de payer la solidarité : la session '%s' n'est pas en cours. (Statut actuel: %s)",
                                        session.getName(),
                                        session.getStatus()
                                )
                        );
                }

                // VÉRIFICATION CRITIQUE : solde impayé suffisant ?
                BigDecimal currentUnpaid = memberAccount.getUnpaidSolidarityAmount();
                if (currentUnpaid == null) {
                        throw new IllegalStateException("Montant impayé de solidarité non initialisé");
                }

                if (amount.compareTo(currentUnpaid) > 0) {
                        throw new IllegalArgumentException(
                                String.format("Le montant saisi (%,.2f) dépasse le solde impayé restant (%,.2f)",
                                        amount, currentUnpaid)
                        );
                }

                //
                // MISE À JOUR DES COMPTES (LOGIQUE FINANCIÈRE)
                //
                // Le membre verse une cotisation de solidarité.
                // On incrémente :
                // - le montant de solidarité du compte membre
                // - le montant de solidarité du compte global de la mutuelle
                memberAccount.setSolidarityAmount(
                                memberAccount.getSolidarityAmount().add(amount));
                memberAccount.setUnpaidSolidarityAmount(
                        memberAccount.getUnpaidSolidarityAmount().subtract(amount));

                globalAccount.setSolidarityAmount(
                                globalAccount.getSolidarityAmount().add(amount));

                // Persistance des nouvelles valeurs
                memberRepo.save(memberAccount);
                memberService.updateMemberStatus(memberAccount);
                globalRepo.save(globalAccount);

                // ─────────────────────────────────────────────
                // 2. CRÉATION DE LA TRANSACTION (TRAÇABILITÉ)
                // ─────────────────────────────────────────────
                // La transaction permet de conserver l'historique
                // des paiements de solidarité par membre et par session.
                Transaction tx = Transaction.builder()
                                .accountMember(memberAccount)
                                .amount(amount)
                                .transactionType(TransactionType.SOLIDARITE)
                                .transactionDirection(TransactionDirection.CREDIT)
                                .session(session)
                                .build();

                // Sauvegarde de la transaction en base
                solidariteRepo.save(tx);
        }

        /**
         * Récupère l'historique des paiements de solidarité d'un membre
         */
        public List<Transaction> getSolidarityHistory(Long memberId) {

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                return solidariteRepo.findByTransactionTypeAndAccountMember(
                                TransactionType.SOLIDARITE,
                                memberAccount);
        }

        /**
         * Calcule le total payé en solidarité par un membre
         */
        public BigDecimal getTotalSolidarityPaid(Long memberId) {

                AccountMember memberAccount = memberRepo.findByMemberId(memberId)
                                .orElseThrow(() -> new RuntimeException("Compte membre introuvable"));

                BigDecimal total = solidariteRepo.sumAmountByTransactionTypeAndAccountMember(
                                TransactionType.SOLIDARITE,
                                memberAccount);

                return total != null ? total : BigDecimal.ZERO;
        }
}
