package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.enums.TemplateMailsName;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.repository.MutuelleConfigRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.repository.TransactionRepository;
import com.mutuelle.mobille.service.notifications.config.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.mutuelle.mobille.enums.NotificationChannel.EMAIL;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final AccountService accountService;
    private final InteretService interetService;
    private final SessionService sessionService;
    private final TransactionRepository transactionRepository;
    private final BorrowingCeilingService borrowingCeilingService;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final AdminService adminService;
    private final MutuelleConfigRepository mutuelleConfigRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public void emprunter(Long memberId, BigDecimal montant) {

        AccountMember emprunteur = accountService.getMemberAccount(memberId);
        Optional<Session> currentSessionOpt = sessionService.findCurrentSession();

        if (currentSessionOpt.isEmpty()) {
            throw new IllegalStateException("Echec : aucune session active en cours");
        }

        Session currentSession = currentSessionOpt.get();

        BigDecimal saving = emprunteur.getSavingAmount() == null
                ? BigDecimal.ZERO
                : emprunteur.getSavingAmount();

        if (saving.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Echec : aucune épargne enregistrée"
            );
        }

        BigDecimal borrow = emprunteur.getBorrowAmount() == null
                ? BigDecimal.ZERO
                : emprunteur.getBorrowAmount();

        if (borrow.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "Echec : un emprunt est déjà en cours"
            );
        }

        BigDecimal plafond = borrowingCeilingService.calculerPlafond(saving);

        if (montant.compareTo(plafond) > 0) {
            throw new IllegalArgumentException(
                    "Montant refusé : plafond autorisé = " + plafond
            );
        }

        BigDecimal interet = interetService.calculerInteret(montant); // valeur de l'interet

        accountService.borrowMoney(memberId, montant, currentSession.getId());

        Transaction trans = transactionRepository.save(
                Transaction.builder()
                        .accountMember(emprunteur)
                        .amount(montant)
                        .transactionType(TransactionType.EMPRUNT)
                        .transactionDirection(TransactionDirection.DEBIT)
                        .session(currentSession)
                        .description("Emprunt de : "+emprunteur.getMember().getLastname())
                        .build()
        );
        BigDecimal currentInteret = currentSession.getTotalInteretAmount() != null
                ? currentSession.getTotalInteretAmount()
                : BigDecimal.ZERO;
        currentSession.setTotalInteretAmount(currentInteret.add(interet));
        sessionRepository.save(currentSession);

        //interetService.redistribuerInteret(emprunteur.getId(), interet,trans,currentSession);
    }

    @Transactional
    public void rembourser(Long memberId, BigDecimal montant) {

        AccountMember membre = accountService.getMemberAccount(memberId);
        Optional<Session> currentSessionOpt = sessionService.findCurrentSession();
        if (currentSessionOpt.isEmpty()) {
            throw new IllegalStateException("Impossible d'effectuer un remboursement : aucune session active en cours");
        }
        Session currentSession = currentSessionOpt.get();

        BigDecimal borrow = membre.getBorrowAmount() == null
                ? BigDecimal.ZERO
                : membre.getBorrowAmount();

        if (borrow.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Aucun emprunt actif à rembourser");
        }

        accountService.repayBorrowedAmount(memberId, montant);

        transactionRepository.save(
                Transaction.builder()
                        .accountMember(membre)
                        .amount(montant)
                        .transactionType(TransactionType.REMBOURSSEMENT)
                        .transactionDirection(TransactionDirection.CREDIT)
                        .session(currentSession)
                        .description("Remboursement emprunt")
                        .build()
        );
    }

    @Transactional
    public void calculerEtRedistribuerInteretsPenalites() {

        Optional<Session> sessionOpt = sessionService.findCurrentSession();
        if (sessionOpt.isEmpty()) {
            return;
        }
        Session session = sessionOpt.get();

        MutuelleConfig config = mutuelleConfigRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Configuration mutuelle introuvable"));

        BigDecimal penaliteFixe = config.getLoanPenaltyFixedAmount();
        boolean penaliteActive = penaliteFixe != null && penaliteFixe.compareTo(BigDecimal.ZERO) > 0;

        int threshold = config.getLoanPenaltySessionThreshold() != null
                ? config.getLoanPenaltySessionThreshold() : 3;

        BigDecimal totalInterets = session.getTotalInteretAmount() != null
                ? session.getTotalInteretAmount()
                : BigDecimal.ZERO;

        List<AccountMember> emprunteurs = accountService.findMembersWithBorrowGreaterThanZero();

        for (AccountMember membreAcc : emprunteurs) {

            Long borrowSessionId = membreAcc.getBorrowSessionId();
            if (borrowSessionId == null) continue;

            Session borrowSession = sessionRepository.findById(borrowSessionId).orElse(null);
            if (borrowSession == null) continue;

            long nbSessionsFermees = sessionRepository.countCompletedSessionsAfter(borrowSession.getStartDate());
            if (nbSessionsFermees < threshold) continue;

            BigDecimal soldeActuel = membreAcc.getBorrowAmount();
            if (soldeActuel == null || soldeActuel.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal interets = interetService.calculerInteret(soldeActuel);
            if (interets.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal montantEquivalent = interetService.calculMontantEmpruntEquivalent(soldeActuel);
            BigDecimal epargneActuelle = membreAcc.getSavingAmount() != null
                    ? membreAcc.getSavingAmount() : BigDecimal.ZERO;
            BigDecimal plafondActuel = borrowingCeilingService.calculerPlafond(epargneActuelle);

            if (montantEquivalent.compareTo(plafondActuel.add(penaliteFixe)) > 0) {

                Optional<AuthUser> authOpt = memberService.getAuthMember(membreAcc.getMember());
                if (authOpt.isEmpty()) continue;
                AuthUser authUser = authOpt.get();

                BigDecimal ecart = montantEquivalent.subtract(plafondActuel);

                Map<String, Object> varsMembre = new HashMap<>();
                varsMembre.put("memberName", membreAcc.getMember().getLastname());
                varsMembre.put("soldeActuel", soldeActuel);
                varsMembre.put("montantEquivalent", montantEquivalent);
                varsMembre.put("plafondActuel", plafondActuel);

                notificationService.sendNotification(NotificationRequestDto.builder()
                        .email(authUser.getEmail())
                        .title("Alerte : Plafond d'emprunt dépassé")
                        .templateName(TemplateMailsName.PLAFOND_DEPASSE_MEMBER)
                        .variables(varsMembre)
                        .channels(Set.of(EMAIL))
                        .build());

                AuthUser authAdmin = adminService.getAuthAdmin();
                if (authAdmin == null) continue;

                Map<String, Object> varsAdmin = new HashMap<>();
                varsAdmin.put("memberName", membreAcc.getMember().getLastname());
                varsAdmin.put("memberId", membreAcc.getMember().getId());
                varsAdmin.put("soldeActuel", soldeActuel);
                varsAdmin.put("montantEquivalent", montantEquivalent);
                varsAdmin.put("plafondActuel", plafondActuel);
                varsAdmin.put("ecart", ecart);
                varsAdmin.put("interets", interets);
                varsAdmin.put("sessionName", session.getName());

                notificationService.sendNotification(NotificationRequestDto.builder()
                        .email(authAdmin.getEmail())
                        .title("[ALERTE] Plafond dépassé – Membre " + membreAcc.getMember().getLastname())
                        .templateName(TemplateMailsName.PLAFOND_DEPASSE_ADMIN)
                        .variables(varsAdmin)
                        .channels(Set.of(EMAIL))
                        .build());

                continue;
            }

            accountService.addBorrowAmount(membreAcc, interets);
            totalInterets = totalInterets.add(interets);

            transactionRepository.save(Transaction.builder()
                    .accountMember(membreAcc)
                    .amount(interets)
                    .transactionType(TransactionType.INTERET)
                    .transactionDirection(TransactionDirection.DEBIT)
                    .session(session)
                    .description("Intérêts trimestriels sur solde restant")
                    .build());

            if (penaliteActive) {
                accountService.addBorrowAmount(membreAcc, penaliteFixe);
                totalInterets = totalInterets.add(penaliteFixe);

                transactionRepository.save(Transaction.builder()
                        .accountMember(membreAcc)
                        .amount(penaliteFixe)
                        .transactionType(TransactionType.PENALITE)
                        .transactionDirection(TransactionDirection.DEBIT)
                        .session(session)
                        .description("Pénalité de retard de remboursement")
                        .build());
            }
        }

        if (totalInterets.compareTo(BigDecimal.ZERO) <= 0) return;

        Transaction totalInteretTrans = transactionRepository.save(Transaction.builder()
                .accountMember(null)
                .amount(totalInterets)
                .transactionType(TransactionType.INTERET)
                .transactionDirection(TransactionDirection.CREDIT)
                .session(session)
                .description("Intérêts globaux redistribués – session " + session.getName())
                .build());

        interetService.redistribuerInteret(totalInterets, totalInteretTrans, session);
    }
//    @Transactional
//    public void appliquerPenalitesEmprunteurs(Session session) {
//        MutuelleConfig config = mutuelleConfigRepository.findTopByOrderByUpdatedAtDesc()
//                .orElseThrow(() -> new IllegalStateException("Configuration mutuelle introuvable"));
//
//        BigDecimal penaliteFixe = config.getLoanPenaltyFixedAmount();
//        if (penaliteFixe == null || penaliteFixe.compareTo(BigDecimal.ZERO) <= 0) return;
//
//        int threshold = config.getLoanPenaltySessionThreshold() != null
//                ? config.getLoanPenaltySessionThreshold() : 3;
//
//        List<AccountMember> emprunteurs = accountService.findMembersWithBorrowGreaterThanZero();
//        for (AccountMember membre : emprunteurs) {
//            Long borrowSessionId = membre.getBorrowSessionId();
//            if (borrowSessionId == null) continue;
//
//            Session borrowSession = sessionRepository.findById(borrowSessionId).orElse(null);
//            if (borrowSession == null) continue;
//
//            long nbSessionsFermees = sessionRepository.countCompletedSessionsAfter(borrowSession.getStartDate());
//            if (nbSessionsFermees < threshold) continue;
//
//            Transaction txPenalite = transactionRepository.save(
//                    Transaction.builder()
//                            .accountMember(membre)
//                            .amount(penaliteFixe)
//                            .transactionType(TransactionType.PENALITE)
//                            .transactionDirection(TransactionDirection.DEBIT)
//                            .session(session)
//                            .description("Pénalité de retard de remboursement")
//                            .build()
//            );
//
//            interetService.redistribuerInteret(  penaliteFixe, txPenalite, session);
//        }
//    }

    private boolean aAtteintProchainTrimestre(LocalDateTime derniere, LocalDateTime actuelle) {
        if (derniere == null) return true;

        LocalDateTime prochain = derniere.plusMonths(3);
        return !actuelle.isBefore(prochain);
    }

    private boolean estNouveauTrimestre(LocalDateTime derniere, LocalDateTime maintenant) {
        int trimestreActuel = getTrimestre(maintenant);
        int trimestreDernier = getTrimestre(derniere);

        return trimestreActuel != trimestreDernier;
    }

    private int getTrimestre(LocalDateTime date) {
        int mois = date.getMonthValue();
        return (mois - 1) / 3 + 1; // 1→3 → T1, 4→6 → T2, etc.
    }
}
