package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.dto.session.SessionRequestDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.dto.session.UpdateSessionRequestDTO;
import com.mutuelle.mobille.enums.*;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.service.notifications.SessionNotificationHelper;
import com.mutuelle.mobille.repository.MemberSessionBilanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final TransactionRepository transactionRepository;
    private final MemberSessionBilanRepository memberSessionBilanRepository;
    private final AccountService accountService;
    private final AssistanceService assistanceService;
    private final SessionNotificationHelper notificationHelper;
    private final InteretService interetService;
    private final BilanService bilanService;

    @Lazy
    @Autowired
    private EmpruntService empruntService;

    @Lazy
    @Autowired
    private MemberComplianceService memberComplianceService;

//    private final Clock clock;  // ← à injecter (configurable pour les tests)

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ───────────────────────────────────────────────
    //              Validation centrale
    // ───────────────────────────────────────────────

    private void validateSessionForCreation(Session session, Long excludeId) {
        Exercice ex = session.getExercice();
        if (ex == null) {
            throw new IllegalArgumentException("Exercice parent obligatoire");
        }
        boolean isExerciceValid = ex.getStatus() == StatusExercice.PLANNED ||
                ex.getStatus() == StatusExercice.IN_PROGRESS;

        if (!isExerciceValid) {
            throw new IllegalStateException(
                    "Impossible de créer une session sur un exercice " + ex.getStatus() +
                            ". L'exercice doit être planifié ou en cours."
            );
        }

        // Pas de validation de dates à la création car elles sont nulles
    }

    private void validateSessionForStart(Session session) {
        // Vérifier qu'aucune autre session n'est en cours
        Optional<Session> currentSession = findCurrentSession();
        if (currentSession.isPresent()) {
            throw new IllegalStateException(
                "Impossible de démarrer la session '" + session.getName() + 
                "' car une session est déjà en cours : '" + currentSession.get().getName() + "'"
            );
        }


        // Vérifier que la session est bien au statut PLANNED
        if (session.getStatus() != StatusSession.PLANNED) {
            throw new IllegalStateException(
                "Seules les sessions planifiées peuvent être démarrées. " +
                "Statut actuel : " + session.getStatus()
            );
        }

        // Vérifier que l'exercice est en cours
        Exercice ex = session.getExercice();
        if (ex.getStatus() != StatusExercice.IN_PROGRESS) {
            throw new IllegalStateException(
                "Impossible de démarrer une session sur un exercice " + ex.getStatus()
            );
        }

        // Vérifier que la date de début est dans les dates de l'exercice
        LocalDateTime now = now();
       /* if (now.isBefore(ex.getStartDate()) || now.isAfter(ex.getEndDate())) {
            throw new IllegalArgumentException(
                "La session ne peut être démarrée que pendant la période de l'exercice.\n" +
                "Période exercice : " + ex.getStartDate() + " → " + ex.getEndDate() + "\n" +
                "Date actuelle : " + now
            );
        }*/



        /*/ Récupérer toutes les sessions démarrées aujourd'hui
        List<Session> todaySessions = sessionRepository.findByStartDateBetween(
                now.toLocalDate().atStartOfDay(),
                now.toLocalDate().atTime(23, 59, 59)
        );

        if (!todaySessions.isEmpty()) {
            Session existingSession = todaySessions.get(0);
            throw new IllegalStateException(
                    String.format(
                            "Impossible de démarrer la session '%s' aujourd'hui. " +
                                    "La session '%s' a déjà été démarrée à %s.",
                            session.getName(),
                            existingSession.getName(),
                            existingSession.getStartDate().toLocalTime()
                    )
            );
        }*/
    }

    private void validateSessionForClose(Session session) {
        if (session.getStartDate() == null) {
            throw new IllegalStateException("La session n'a pas de date de début valide");
        }

        if (session.getStatus() != StatusSession.IN_PROGRESS) {
            throw new IllegalStateException("Seules les sessions en cours peuvent être clôturées");
        }

        //LocalDateTime now = now();
        //if (now.isBefore(session.getStartDate())) {
          //  throw new IllegalStateException("La session ne peut être clôturée avant sa date de début");
        //}
    }
    
    private void validateSession(Session session, Long excludeId) {
        LocalDateTime start = session.getStartDate();
        LocalDateTime end = session.getEndDate();
        LocalDateTime now = now();

        {/*if (end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Date de début doit être ≤ date de fin");
            }

            if (start.toLocalDate().equals(end.toLocalDate())) {
                throw new IllegalArgumentException(
                        "La session doit couvrir au moins deux jours " +
                                "(date de début et date de fin ne peuvent pas être le même jour)"
                );
            }
        }*/}

        Exercice ex = session.getExercice();
        if (ex == null) {
            throw new IllegalArgumentException("Exercice parent obligatoire");
        }

        if (start != null) { // seulement si start est défini
            // Session doit être contenue dans l'exercice
            if (start.isBefore(ex.getStartDate()) || start.isAfter(ex.getEndDate()) ||
                    (ex.getEndDate() != null && end != null && end.isAfter(ex.getEndDate()))) {
                throw new IllegalArgumentException(
                        "La session doit être contenue dans l'exercice → " +
                                "date de début ne peut pas être antérieure au début de l'exercice"
                );
            }
        }
        // Pas de chevauchement avec d'autres sessions (tous statuts confondus)
        boolean overlap = sessionRepository.existsOverlapping(
                start,
                end != null ? end : LocalDateTime.MAX,
                excludeId
        );

        if (overlap) {
            throw new IllegalArgumentException("les plages de date de la session coincide avec une autre ");
        }

        // Si la session est ou devient IN_PROGRESS → vérifier qu'aucune autre ne l'est déjà
        boolean wouldBeInProgress = !start.isAfter(now) && (end == null || !end.isBefore(now));
        if (wouldBeInProgress && excludeId != null) {
            Optional<Session> other = sessionRepository.findFirstByStatus(StatusSession.IN_PROGRESS);
            if (other.isPresent() && !other.get().getId().equals(excludeId)) {
                throw new IllegalStateException("Une autre session est déjà IN_PROGRESS");
            }
        }

        // Interdire modification si déjà historisée
        if (excludeId != null && sessionHistoryRepository.existsBySessionId(excludeId)) {
            throw new IllegalStateException("Session déjà clôturée (historique existant)");
        }
    }

    // ───────────────────────────────────────────────
    //              CRUD de base
    // ───────────────────────────────────────────────

    public List<SessionResponseDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SessionResponseDTO getSessionById(Long id) {
        Session s = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + id));
        return toResponseDTO(s);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO createSession(SessionRequestDTO dto) {
        Exercice ex = exerciceRepository.findById(dto.getExerciceId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé : " + dto.getExerciceId()));

        Session session = Session.builder()
                .name(dto.getName())
                //.solidarityAmount(dto.getSolidarityAmount())
                .agapeAmountPerMember(dto.getAgapeAmountPerMember())
                .startDate(null)
                .endDate(null)
                .status(StatusSession.PLANNED)
                .exercice(ex)
                .build();

        validateSessionForCreation(session, null);
        session = sessionRepository.save(session);
        return toResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO updateSession(Long id, UpdateSessionRequestDTO dto) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + id));

        StatusSession currentStatus = session.getStatus();


        if (currentStatus == StatusSession.COMPLETED || currentStatus == StatusSession.CANCELLED) {
            throw new IllegalStateException("Modification interdite sur session terminée ou annulée");
        }

        // Interdire modification si déjà historisée
        if (sessionHistoryRepository.existsBySessionId(id)) {
            throw new IllegalStateException("Session déjà clôturée (historique existant)");
        }

        /*/ montants positifs
        if (dto.getSolidarityAmount() != null && dto.getSolidarityAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant de solidarité doit être strictement positif");
        }*/

        if (dto.getAgapeAmountPerMember() != null && dto.getAgapeAmountPerMember().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant de l'agape ne peut pas être négatif");
        }

        /*/  Vérifier si la session a des transactions
        Long transactionCount = transactionRepository.countBySessionId(id);

        if (transactionCount > 0) {
            // Session avec transactions : message clair
            throw new IllegalStateException(
                    String.format(
                            "Impossible de modifier la session '%s' car elle a déjà %d transaction(s). " +
                                    "Les modifications ne sont autorisées que sur les sessions sans activité.",
                            session.getName(),
                            transactionCount
                    )
            );
        }*/

        // Sauvegarder l'ancien montant avant modification
       // BigDecimal oldSolidarityAmount = session.getSolidarityAmount();

        // Mise à jour des champs modifiables
        if (dto.getName() != null) session.setName(dto.getName());
        //if (dto.getSolidarityAmount() != null) session.setSolidarityAmount(dto.getSolidarityAmount());
        if (dto.getAgapeAmountPerMember() != null) session.setAgapeAmountPerMember(dto.getAgapeAmountPerMember());



        // L'exercice ne peut pas être changé
        /*if (dto.getExerciceId() != null && !dto.getExerciceId().equals(session.getExercice().getId())) {
            throw new IllegalArgumentException("L'exercice d'une session ne peut pas être modifié");
        }*/

        {/*/ Gestion stricte des dates
        if (currentStatus == StatusSession.IN_PROGRESS) {
            // startDate interdit
            if (dto.getStartDate() != null && !dto.getStartDate().equals(session.getStartDate())) {
                throw new IllegalArgumentException("Modification de startDate interdite sur session en cours");
            }
            // endDate → prolongation uniquement
            if (dto.getEndDate() != null) {
                LocalDateTime newEnd = dto.getEndDate();
                if (newEnd.isBefore(session.getEndDate())) {
                    throw new IllegalArgumentException("Raccourcissement de session en cours interdit");
                }
                if (newEnd.isBefore(now())) {
                    throw new IllegalArgumentException("Nouvelle fin doit être dans le futur");
                }
                session.setEndDate(newEnd);
            }
        } else {
            if (dto.getStartDate() != null) session.setStartDate(dto.getStartDate());
            if (dto.getEndDate() != null) session.setEndDate(dto.getEndDate());
        }

        Session fakeClone = Session.builder()
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : session.getStartDate())
                .endDate(dto.getEndDate() != null ? dto.getEndDate() : session.getEndDate())
                .build();

        validateSession(fakeClone, id);*/}

        session = sessionRepository.save(session);
        // Si le montant de solidarité a changé et que la session est en cours
        /*if (currentStatus == StatusSession.IN_PROGRESS &&
                dto.getSolidarityAmount() != null &&
                dto.getSolidarityAmount().compareTo(oldSolidarityAmount) != 0) {

            BigDecimal difference = dto.getSolidarityAmount().subtract(oldSolidarityAmount);

            // Ajuster les unpaid de tous les membres (positif ou négatif)
            adjustUnpaidSolidarityForAllMembers(difference);
        }*/
        return toResponseDTO(session);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSession(Long id) {
        Session s = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + id));

        //Nouvelle règle : Interdire si en cours ou terminée
        if ( s.getStatus() == StatusSession.COMPLETED) {
            throw new IllegalStateException("Impossible de supprimer une session terminée");
        }

        // Sécurité supplémentaire existante (historique)
        if (sessionHistoryRepository.existsBySessionId(id)) {
            throw new IllegalStateException("Impossible de supprimer une session historisée");
        }
        //  Vérifier si la session a des transactions
        Long transactionCount = transactionRepository.countBySessionId(id);

        if (transactionCount > 0) {
            // Session avec transactions : message clair
            throw new IllegalStateException(
                    String.format(
                            "Impossible de supprimer la session '%s' car elle a déjà %d transaction(s). " +
                                    "Les suppressions de sessions ne sont autorisées que sur les sessions sans activité.",
                            s.getName(),
                            transactionCount
                    )
            );
        }

        sessionRepository.delete(s);
    }

    // ───────────────────────────────────────────────
    //              Méthodes lecture / état
    // ───────────────────────────────────────────────

    public Optional<Session> findCurrentSession() {
        return sessionRepository.findFirstByStatus(StatusSession.IN_PROGRESS);
    }

    public Optional<SessionResponseDTO> getCurrentSessionDTO() {
        return findCurrentSession().map(this::toResponseDTO);
    }

    // ───────────────────────────────────────────────
    //              Actions importantes (appelées par scheduler)
    // ───────────────────────────────────────────────

    //Demarrage automatique
    /*@Transactional
    public void startSessionIfDue(Session session) {
        if (session.getStatus() != StatusSession.PLANNED) return;

        LocalDateTime n = now();
        if (!session.getStartDate().isAfter(n)) {
            session.setStatus(StatusSession.IN_PROGRESS);
            sessionRepository.save(session);
            applySolidarityToAllMembers(session);
            notificationHelper.notifySessionStarted(session);
        }
    }*/

    //Demarrage manuel
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO startSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + sessionId));

        validateSessionForStart(session);

        session.setStartDate(LocalDateTime.now());
        session.setStatus(StatusSession.IN_PROGRESS);

        
        session = sessionRepository.save(session);

        // Appliquer la solidarité à tous les membres
       // applySolidarityToAllMembers(session);
        // Notifier
        notificationHelper.notifySessionStarted(session);
        return toResponseDTO(session);
    }

    //cloture automatique
    {/*@Transactional
    public void closeSessionIfExpired(Session session) {
        if (session.getStatus() != StatusSession.IN_PROGRESS) return;

        LocalDateTime n = now();
        if (session.getEndDate() != null && session.getEndDate().isBefore(n)) {
//            session.setStatus(StatusSession.COMPLETED);
//            sessionRepository.save(session);
//            onSessionEnded(session);
            try {
                session.setStatus(StatusSession.COMPLETED);
                onSessionEnded(session);

                // Notification de fin de session
                notificationHelper.notifySessionEnded(session);

            } catch (IllegalArgumentException e) {
                session.setStatus(StatusSession.IN_PROGRESS);
                sessionRepository.save(session);

                notificationHelper.notifyAdminCritical(
                        "Échec clôture session " + session.getName(),
                        "Impossible de clôturer - caisse solidarité insuffisante",
                        e
                );

                throw e; // on laisse remonter l'exception
            }
        }
    }*/}

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO closeSession(Long sessionId) {
        return closeSession(sessionId, false);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponseDTO closeSession(Long sessionId, Boolean deductAgape) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée : " + sessionId));

        validateSessionForClose(session);

        try {
            onSessionEnded(session, Boolean.TRUE.equals(deductAgape));
            session.setEndDate(LocalDateTime.now());
            session.setStatus(StatusSession.COMPLETED);
            session = sessionRepository.save(session);
            notificationHelper.notifySessionEnded(session);

            return toResponseDTO(session);
        } catch (IllegalArgumentException e) {
            notificationHelper.notifyAdminCritical(
                    "Échec clôture session " + session.getName(),
                    "Impossible de clôturer la session",
                    e
            );
            throw e;
        }
    }


   /* @Transactional
    public void applySolidarityToAllMembers(Session session) {
        if (session.getSolidarityAmount() == null || session.getSolidarityAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<AccountMember> activeMembers = accountService.getAllMemberAccounts().stream()
                .filter(AccountMember::isActive)
                .toList();

        for (AccountMember m : activeMembers) {
            BigDecimal unpaid = m.getUnpaidSolidarityAmount();
            if (unpaid == null) unpaid = BigDecimal.ZERO;
            m.setUnpaidSolidarityAmount(unpaid.add(session.getSolidarityAmount()));
            accountService.saveMemberAccount(m);
        }

//        notificationHelper.notifySolidarityApplied(session);
    }
    @Transactional
    private void adjustUnpaidSolidarityForAllMembers(BigDecimal difference) {
        List<AccountMember> activeMembers = accountService.getAllMemberAccounts().stream()
                .filter(AccountMember::isActive)
                .toList();

        for (AccountMember m : activeMembers) {
            BigDecimal unpaid = m.getUnpaidSolidarityAmount();
            if (unpaid == null) unpaid = BigDecimal.ZERO;
            m.setUnpaidSolidarityAmount(unpaid.add(difference));
            accountService.saveMemberAccount(m);
        }
    }*/

    @Transactional
    public void onSessionEnded(Session session) {
        onSessionEnded(session, false);
    }

    @Transactional
    public void onSessionEnded(Session session, boolean deductAgape) {
        if (session.getHistory() != null) return;

        // Redistribuer les intérêts accumulés à l'octroi des prêts pendant cette session
        BigDecimal interetAccumule = session.getTotalInteretAmount() != null
                ? session.getTotalInteretAmount()
                : BigDecimal.ZERO;

        if (interetAccumule.compareTo(BigDecimal.ZERO) > 0) {
            // Créer la transaction globale d'intérêt
            Transaction interetParent = Transaction.builder()
                    .transactionType(TransactionType.INTERET)
                    .transactionDirection(TransactionDirection.CREDIT)
                    .amount(interetAccumule)
                    .description("Redistribution intérêts session " + session.getName())
                    .accountMember(null)
                    .session(session)
                    .build();
            Transaction savedInteretParent = transactionRepository.save(interetParent);

            // Redistribuer aux membres légitimes (solidarité et renflouement à jour)
            interetService.redistribuerInteret(interetAccumule, savedInteretParent, session);
        }

        // Pénalités sur emprunts en retard (séparées des intérêts d'octroi)
        empruntService.appliquerPenalites(session);

        AccountMutuelle mutuelleacc = accountService.getMutuelleGlobalAccount();
        Long sessionId = session.getId();

        BigDecimal agapeAmount = BigDecimal.ZERO;
        if (deductAgape) {
            agapeAmount = session.getAgapeAmountPerMember() != null
                    ? session.getAgapeAmountPerMember() : BigDecimal.ZERO;

            if (agapeAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentRegistrationBalance = mutuelleacc.getRegistrationAmount();
                if (currentRegistrationBalance.compareTo(agapeAmount) < 0) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Impossible de débiter les agapes pour la session '%s' : " +
                                            "caisse inscription insuffisante.\n" +
                                            "→ Montant requis : %s\n" +
                                            "→ Solde actuel  : %s\n" +
                                            "→ Écart         : %s\n",
                                    session.getName(),
                                    agapeAmount,
                                    currentRegistrationBalance,
                                    agapeAmount.subtract(currentRegistrationBalance)
                            )
                    );
                }

                accountService.removeToRegistrationMutuelleCaisse(agapeAmount);

                Transaction tx = Transaction.builder()
                        .transactionType(TransactionType.AGAPE)
                        .amount(agapeAmount)
                        .description("Agapes session " + session.getName())
                        .transactionDirection(TransactionDirection.DEBIT)
                        .accountMember(null)
                        .session(session)
                        .build();
                transactionRepository.save(tx);

                mutuelleacc = accountService.getMutuelleGlobalAccount();
            }
        }

        memberComplianceService.onSessionClosed(session);

        // Agrégation des transactions de la session
        BigDecimal totalSolidarityCollected  = transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.SOLIDARITE,     TransactionDirection.CREDIT);
        Long       totalSolidarityCount      = transactionRepository.countBySessionAndTypeAndDirection(sessionId, TransactionType.SOLIDARITE,    TransactionDirection.CREDIT);
        BigDecimal totalEpargneDeposited     = transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.EPARGNE,         TransactionDirection.CREDIT);
        BigDecimal totalEpargneWithdrawn     = transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.EPARGNE,         TransactionDirection.DEBIT);
        BigDecimal totalEmpruntAmount        = transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.EMPRUNT,         TransactionDirection.DEBIT);
        BigDecimal totalRemboursementAmount  = transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.REMBOURSSEMENT,  TransactionDirection.CREDIT);
        BigDecimal totalInteretAmount        = transactionRepository.sumBySessionAndTypeAndDirectionWithoutMemberNoParent(sessionId, TransactionType.INTERET, TransactionDirection.CREDIT);
        BigDecimal totalRenfoulementCollected= transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.RENFOULEMENT,    TransactionDirection.CREDIT);
        BigDecimal totalRegistrationCollected= transactionRepository.sumBySessionAndTypeAndDirection(sessionId, TransactionType.INSCRIPTION,     TransactionDirection.CREDIT);
        long activeMembersCount              = accountService.getAllMemberAccountsWithActive(true).size();

        // Création de l'historique de session
        SessionHistory history = SessionHistory.builder()
                .session(session)
                .totalAssistanceAmount(assistanceService.getTotalAssistanceAmountForSession(sessionId))
                .totalAssistanceCount(assistanceService.countTotalAssistanceForSession(sessionId))
                .agapeAmount(agapeAmount)
                .totalTransactions(transactionRepository.countBySessionId(sessionId))
                .totalSolidarityCollected(totalSolidarityCollected)
                .totalSolidarityCount(totalSolidarityCount)
                .totalEpargneDeposited(totalEpargneDeposited)
                .totalEpargneWithdrawn(totalEpargneWithdrawn)
                .totalEmpruntAmount(totalEmpruntAmount)
                .totalRemboursementAmount(totalRemboursementAmount)
                .totalInteretAmount(totalInteretAmount)
                .totalRenfoulementCollected(totalRenfoulementCollected)
                .totalRegistrationCollected(totalRegistrationCollected)
                .activeMembersCount(activeMembersCount)
                .mutuelleCash(mutuelleacc.getSavingAmount()
                        .add(mutuelleacc.getSolidarityAmount())
                        .add(mutuelleacc.getRegistrationAmount()))
                .mutuellesSavingAmount(mutuelleacc.getSavingAmount())
                .mutuelleSolidarityAmount(mutuelleacc.getSolidarityAmount())
                .mutuelleRegistrationAmount(mutuelleacc.getRegistrationAmount())
                .mutuelleBorrowAmount(mutuelleacc.getBorrowAmount())
                .build();

        session.setHistory(history);

        // Création des bilans membres pour cette session
        List<AccountMember> activeAccounts = accountService.getAllMemberAccountsWithActive(true);
        bilanService.createMemberSessionBilans(session, activeAccounts);
    }

    // ───────────────────────────────────────────────
    //         Rollback réouverture de session
    // ───────────────────────────────────────────────

    @Transactional
    public void rollbackOnSessionReopened(Session session) {
        Long sessionId = session.getId();

        // 1. Reverse INTERET DEBIT per borrower → subtract from borrowAmount
        List<Transaction> interetDebits = transactionRepository
                .findBySessionIdAndTypeAndDirectionWithMember(sessionId, TransactionType.INTERET, TransactionDirection.DEBIT);
        for (Transaction tx : interetDebits) {
            accountService.subtractBorrowAmount(tx.getAccountMember(), tx.getAmount());
        }

        // 2. Reverse PENALITE DEBIT per borrower → subtract from borrowAmount
        List<Transaction> penaliteDebits = transactionRepository
                .findBySessionIdAndTypeAndDirectionWithMember(sessionId, TransactionType.PENALITE, TransactionDirection.DEBIT);
        for (Transaction tx : penaliteDebits) {
            accountService.subtractBorrowAmount(tx.getAccountMember(), tx.getAmount());
        }

        // 3. Reverse interest redistribution to member savings
        //    Find global INTERET CREDIT (parent, no accountMember)
        List<Transaction> globalInteretCredits = transactionRepository
                .findBySessionIdAndTypeAndDirectionWithoutMemberNoParent(sessionId, TransactionType.INTERET, TransactionDirection.CREDIT);
        for (Transaction parent : globalInteretCredits) {
            List<Transaction> children = transactionRepository.findByParentTransactionId(parent.getId());
            for (Transaction child : children) {
                if (child.getAccountMember() != null) {
                    // Was added to member saving + global saving
                    accountService.subtractMemberSavingAmount(child.getAccountMember(), child.getAmount());
                } else {
                    // Reliquat → was added only to global saving (caisse)
                    accountService.subtractFromGlobalSaving(child.getAmount());
                }
            }
            // Delete children first to avoid FK constraint
            transactionRepository.deleteAll(children);
        }
        // Delete parent INTERET CREDIT transactions
        transactionRepository.deleteAll(globalInteretCredits);

        // 4. Delete INTERET and PENALITE DEBIT transactions per member
        transactionRepository.deleteAll(interetDebits);
        transactionRepository.deleteAll(penaliteDebits);

        // 5. Reverse AGAPE DEBIT → credit back AccountMutuelle.registrationAmount
        List<Transaction> agapeDebits = transactionRepository
                .findBySessionIdAndTypeAndDirectionWithoutMemberNoParent(sessionId, TransactionType.AGAPE, TransactionDirection.DEBIT);
        for (Transaction tx : agapeDebits) {
            accountService.addToRegistrationMutuelleCaisse(tx.getAmount());
        }
        transactionRepository.deleteAll(agapeDebits);

        // 6. Delete MemberSessionBilans for this session
        memberSessionBilanRepository.deleteAll(
                memberSessionBilanRepository.findBySessionId(sessionId)
        );

        // 7. Delete SessionHistory (disassociate first to trigger orphanRemoval)
        if (session.getHistory() != null) {
            session.setHistory(null);
            sessionRepository.save(session);
        }

        // 8. Reopen session
        session.setStatus(StatusSession.IN_PROGRESS);
        session.setEndDate(null);
        sessionRepository.save(session);

        log.info("Session '{}' réouverte avec succès (rollback effectué)", session.getName());
    }

    public SessionResponseDTO toResponseDTO(Session s) {
        return SessionResponseDTO.builder()
                .id(s.getId())
                .name(s.getName())
                //.solidarityAmount(s.getSolidarityAmount())
                .agapeAmountPerMember(s.getAgapeAmountPerMember())
                .totalInteretAmount(s.getTotalInteretAmount() != null ? s.getTotalInteretAmount() : java.math.BigDecimal.ZERO)
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .exerciceId(s.getExercice().getId())
                .exerciceName(s.getExercice().getName())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

}