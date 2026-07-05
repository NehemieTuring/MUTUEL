package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.bilan.MemberExerciceBilanDTO;
import com.mutuelle.mobille.dto.bilan.MemberSessionBilanDTO;
import com.mutuelle.mobille.dto.exercice.ExerciceHistoryDto;
import com.mutuelle.mobille.dto.sessionHistory.SessionHistoryResponseDTO;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.models.Exercice;
import com.mutuelle.mobille.models.ExerciceHistory;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.bilan.MemberExerciceBilan;
import com.mutuelle.mobille.models.bilan.MemberSessionBilan;
import com.mutuelle.mobille.repository.ExerciceHistoryRepository;
import com.mutuelle.mobille.repository.MemberExerciceBilanRepository;
import com.mutuelle.mobille.repository.MemberSessionBilanRepository;
import com.mutuelle.mobille.repository.SessionHistoryRepository;
import com.mutuelle.mobille.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BilanService {

    private final MemberSessionBilanRepository memberSessionBilanRepository;
    private final MemberExerciceBilanRepository memberExerciceBilanRepository;
    private final TransactionRepository transactionRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final ExerciceHistoryRepository exerciceHistoryRepository;

    // ─────────────────────────────────────────────────────────────────────────
    //  CRÉATION (appelée depuis SessionService et ExerciceService)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void createMemberSessionBilans(Session session, List<AccountMember> activeAccounts) {
        Long sessionId = session.getId();

        for (AccountMember compte : activeAccounts) {
            Member member = compte.getMember();
            if (member == null) continue;
            if (memberSessionBilanRepository.existsByMemberIdAndSessionId(member.getId(), sessionId)) continue;

            Long accountMemberId = compte.getId();

            BigDecimal solidaritePaid      = sum(sessionId, accountMemberId, TransactionType.SOLIDARITE,     TransactionDirection.CREDIT);
            BigDecimal epargneDeposited    = sum(sessionId, accountMemberId, TransactionType.EPARGNE,        TransactionDirection.CREDIT);
            BigDecimal epargneWithdrawn    = sum(sessionId, accountMemberId, TransactionType.EPARGNE,        TransactionDirection.DEBIT);
            BigDecimal registrationPaid    = sum(sessionId, accountMemberId, TransactionType.INSCRIPTION,    TransactionDirection.CREDIT);
            BigDecimal renfoulementPaid    = sum(sessionId, accountMemberId, TransactionType.RENFOULEMENT,   TransactionDirection.CREDIT);
            BigDecimal remboursement       = sum(sessionId, accountMemberId, TransactionType.REMBOURSSEMENT, TransactionDirection.CREDIT);
            BigDecimal empruntAmount       = sum(sessionId, accountMemberId, TransactionType.EMPRUNT,        TransactionDirection.DEBIT);
            BigDecimal interetAmount       = sum(sessionId, accountMemberId, TransactionType.INTERET,        TransactionDirection.CREDIT);
            BigDecimal assistanceReceived  = sum(sessionId, accountMemberId, TransactionType.ASSISTANCE,     TransactionDirection.DEBIT);
            BigDecimal agapeShare          = BigDecimal.ZERO;

            MemberSessionBilan bilan = MemberSessionBilan.builder()
                    .member(member)
                    .session(session)
                    .solidaritePaid(solidaritePaid)
                    .epargneDeposited(epargneDeposited)
                    .epargneWithdrawn(epargneWithdrawn)
                    .registrationPaid(registrationPaid)
                    .renfoulementPaid(renfoulementPaid)
                    .remboursementAmount(remboursement)
                    .empruntAmount(empruntAmount)
                    .interetAmount(interetAmount)
                    .assistanceReceived(assistanceReceived)
                    .agapeShare(agapeShare)
                    .snapshotSavingAmount(orZero(compte.getSavingAmount()))
                    .snapshotBorrowAmount(orZero(compte.getBorrowAmount()))
                    .snapshotUnpaidSolidarity(orZero(compte.getUnpaidSolidarityAmount()))
                    .snapshotUnpaidRegistration(orZero(compte.getUnpaidRegistrationAmount()))
                    .snapshotUnpaidRenfoulement(orZero(compte.getUnpaidRenfoulement()))
                    .build();

            memberSessionBilanRepository.save(bilan);
        }

        log.info("MemberSessionBilan créés pour session {} ({} membres)", sessionId, activeAccounts.size());
    }

    @Transactional
    public void createMemberExerciceBilans(Exercice exercice,
                                            List<Member> activeMembers,
                                            List<Session> sessions,
                                            BigDecimal renfoulementUnitaire) {
        Long exerciceId = exercice.getId();
        int sessionsCount = sessions.size();

        BigDecimal totalAgapeShareBase = BigDecimal.ZERO;

        for (Member member : activeMembers) {
            AccountMember compte = member.getAccountMember();
            if (compte == null) continue;
            if (memberExerciceBilanRepository.existsByMemberIdAndExerciceId(member.getId(), exerciceId)) continue;

            Long accountMemberId = compte.getId();

            BigDecimal totalSolidaritePaid    = sumEx(exerciceId, accountMemberId, TransactionType.SOLIDARITE,     TransactionDirection.CREDIT);
            BigDecimal totalEpargneDeposited  = sumEx(exerciceId, accountMemberId, TransactionType.EPARGNE,        TransactionDirection.CREDIT);
            BigDecimal totalEpargneWithdrawn  = sumEx(exerciceId, accountMemberId, TransactionType.EPARGNE,        TransactionDirection.DEBIT);
            BigDecimal totalRegistrationPaid  = sumEx(exerciceId, accountMemberId, TransactionType.INSCRIPTION,    TransactionDirection.CREDIT);
            BigDecimal totalRenfoulementPaid  = sumEx(exerciceId, accountMemberId, TransactionType.RENFOULEMENT,   TransactionDirection.CREDIT);
            BigDecimal totalRemboursement     = sumEx(exerciceId, accountMemberId, TransactionType.REMBOURSSEMENT, TransactionDirection.CREDIT);
            BigDecimal totalEmpruntAmount     = sumEx(exerciceId, accountMemberId, TransactionType.EMPRUNT,        TransactionDirection.DEBIT);
            BigDecimal totalInteretAmount     = sumEx(exerciceId, accountMemberId, TransactionType.INTERET,        TransactionDirection.CREDIT);
            BigDecimal totalAssistance        = sumEx(exerciceId, accountMemberId, TransactionType.ASSISTANCE,     TransactionDirection.DEBIT);

            MemberExerciceBilan bilan = MemberExerciceBilan.builder()
                    .member(member)
                    .exercice(exercice)
                    .totalSolidaritePaid(totalSolidaritePaid)
                    .totalEpargneDeposited(totalEpargneDeposited)
                    .totalEpargneWithdrawn(totalEpargneWithdrawn)
                    .totalRegistrationPaid(totalRegistrationPaid)
                    .totalRenfoulementPaid(totalRenfoulementPaid)
                    .totalRemboursementAmount(totalRemboursement)
                    .totalEmpruntAmount(totalEmpruntAmount)
                    .totalInteretAmount(totalInteretAmount)
                    .totalAssistanceReceived(totalAssistance)
                    .totalAgapeShare(totalAgapeShareBase)
                    .renfoulementDistributed(orZero(renfoulementUnitaire))
                    .sessionsCount(sessionsCount)
                    .snapshotSavingAmount(orZero(compte.getSavingAmount()))
                    .snapshotBorrowAmount(orZero(compte.getBorrowAmount()))
                    .snapshotUnpaidSolidarity(orZero(compte.getUnpaidSolidarityAmount()))
                    .snapshotUnpaidRegistration(orZero(compte.getUnpaidRegistrationAmount()))
                    .snapshotUnpaidRenfoulement(orZero(compte.getUnpaidRenfoulement()))
                    .build();

            memberExerciceBilanRepository.save(bilan);
        }

        log.info("MemberExerciceBilan créés pour exercice {} ({} membres)", exerciceId, activeMembers.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LECTURE - Bilan membre
    // ─────────────────────────────────────────────────────────────────────────

    public MemberSessionBilanDTO getMemberSessionBilan(Long memberId, Long sessionId) {
        MemberSessionBilan b = memberSessionBilanRepository.findByMemberIdAndSessionId(memberId, sessionId)
                .orElseThrow(() -> new RuntimeException("Bilan session introuvable pour ce membre/session"));
        return toDTO(b);
    }

    public MemberExerciceBilanDTO getMemberExerciceBilan(Long memberId, Long exerciceId) {
        MemberExerciceBilan b = memberExerciceBilanRepository.findByMemberIdAndExerciceId(memberId, exerciceId)
                .orElseThrow(() -> new RuntimeException("Bilan exercice introuvable pour ce membre/exercice"));
        return toDTO(b);
    }

    public List<MemberSessionBilanDTO> getMemberAllSessionBilans(Long memberId) {
        return memberSessionBilanRepository.findByMemberId(memberId).stream().map(this::toDTO).toList();
    }

    public List<MemberExerciceBilanDTO> getMemberAllExerciceBilans(Long memberId) {
        return memberExerciceBilanRepository.findByMemberId(memberId).stream().map(this::toDTO).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LECTURE - Vue admin (tous les membres)
    // ─────────────────────────────────────────────────────────────────────────

    public List<MemberSessionBilanDTO> getAllMemberBilansBySession(Long sessionId) {
        return memberSessionBilanRepository.findBySessionId(sessionId).stream().map(this::toDTO).toList();
    }

    public List<MemberExerciceBilanDTO> getAllMemberBilansByExercice(Long exerciceId) {
        return memberExerciceBilanRepository.findByExerciceId(exerciceId).stream().map(this::toDTO).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LECTURE - Bilan mutuelle
    // ─────────────────────────────────────────────────────────────────────────

    public SessionHistoryResponseDTO getMutuelleSessionBilan(Long sessionId) {
        SessionHistory h = sessionHistoryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Historique session introuvable : " + sessionId));
        return toDTO(h);
    }

    public ExerciceHistoryDto getMutuelleExerciceBilan(Long exerciceId) {
        ExerciceHistory h = exerciceHistoryRepository.findByExerciceId(exerciceId)
                .orElseThrow(() -> new RuntimeException("Historique exercice introuvable : " + exerciceId));
        return toDTO(h);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Mappers
    // ─────────────────────────────────────────────────────────────────────────

    public MemberSessionBilanDTO toDTO(MemberSessionBilan b) {
        BigDecimal totalVerse = b.getSolidaritePaid()
                .add(b.getEpargneDeposited())
                .add(b.getRegistrationPaid())
                .add(b.getRenfoulementPaid())
                .add(b.getRemboursementAmount());

        BigDecimal totalRecu = b.getEpargneWithdrawn()
                .add(b.getEmpruntAmount())
                .add(b.getInteretAmount());

        return MemberSessionBilanDTO.builder()
                .id(b.getId())
                .memberId(b.getMember().getId())
                .memberFirstname(b.getMember().getFirstname())
                .memberLastname(b.getMember().getLastname())
                .sessionId(b.getSession().getId())
                .sessionName(b.getSession().getName())
                .exerciceName(b.getSession().getExercice().getName())
                .sessionStartDate(b.getSession().getStartDate())
                .sessionEndDate(b.getSession().getEndDate())
                .solidaritePaid(b.getSolidaritePaid())
                .epargneDeposited(b.getEpargneDeposited())
                .epargneWithdrawn(b.getEpargneWithdrawn())
                .registrationPaid(b.getRegistrationPaid())
                .renfoulementPaid(b.getRenfoulementPaid())
                .remboursementAmount(b.getRemboursementAmount())
                .empruntAmount(b.getEmpruntAmount())
                .interetAmount(b.getInteretAmount())
                .assistanceReceived(b.getAssistanceReceived())
                .agapeShare(b.getAgapeShare())
                .totalVerse(totalVerse)
                .totalRecu(totalRecu)
                .netSession(totalVerse.subtract(totalRecu))
                .snapshotSavingAmount(b.getSnapshotSavingAmount())
                .snapshotBorrowAmount(b.getSnapshotBorrowAmount())
                .snapshotUnpaidSolidarity(b.getSnapshotUnpaidSolidarity())
                .snapshotUnpaidRegistration(b.getSnapshotUnpaidRegistration())
                .snapshotUnpaidRenfoulement(b.getSnapshotUnpaidRenfoulement())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public MemberExerciceBilanDTO toDTO(MemberExerciceBilan b) {
        BigDecimal totalVerse = b.getTotalSolidaritePaid()
                .add(b.getTotalEpargneDeposited())
                .add(b.getTotalRegistrationPaid())
                .add(b.getTotalRenfoulementPaid())
                .add(b.getTotalRemboursementAmount());

        BigDecimal totalRecu = b.getTotalEpargneWithdrawn()
                .add(b.getTotalEmpruntAmount())
                .add(b.getRenfoulementDistributed());

        return MemberExerciceBilanDTO.builder()
                .id(b.getId())
                .memberId(b.getMember().getId())
                .memberFirstname(b.getMember().getFirstname())
                .memberLastname(b.getMember().getLastname())
                .exerciceId(b.getExercice().getId())
                .exerciceName(b.getExercice().getName())
                .exerciceStartDate(b.getExercice().getStartDate())
                .exerciceEndDate(b.getExercice().getEndDate())
                .sessionsCount(b.getSessionsCount())
                .totalSolidaritePaid(b.getTotalSolidaritePaid())
                .totalEpargneDeposited(b.getTotalEpargneDeposited())
                .totalEpargneWithdrawn(b.getTotalEpargneWithdrawn())
                .totalRegistrationPaid(b.getTotalRegistrationPaid())
                .totalRenfoulementPaid(b.getTotalRenfoulementPaid())
                .totalRemboursementAmount(b.getTotalRemboursementAmount())
                .totalEmpruntAmount(b.getTotalEmpruntAmount())
                .totalInteretAmount(b.getTotalInteretAmount())
                .totalAssistanceReceived(b.getTotalAssistanceReceived())
                .totalAgapeShare(b.getTotalAgapeShare())
                .renfoulementDistributed(b.getRenfoulementDistributed())
                .totalVerse(totalVerse)
                .totalRecu(totalRecu)
                .netExercice(totalVerse.subtract(totalRecu))
                .snapshotSavingAmount(b.getSnapshotSavingAmount())
                .snapshotBorrowAmount(b.getSnapshotBorrowAmount())
                .snapshotUnpaidSolidarity(b.getSnapshotUnpaidSolidarity())
                .snapshotUnpaidRegistration(b.getSnapshotUnpaidRegistration())
                .snapshotUnpaidRenfoulement(b.getSnapshotUnpaidRenfoulement())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public SessionHistoryResponseDTO toDTO(SessionHistory h) {
        return SessionHistoryResponseDTO.builder()
                .id(h.getId())
                .sessionId(h.getSession().getId())
                .sessionName(h.getSession().getName())
                .exerciceName(h.getSession().getExercice().getName())
                .sessionStartDate(h.getSession().getStartDate())
                .sessionEndDate(h.getSession().getEndDate())
                .totalAssistanceAmount(h.getTotalAssistanceAmount())
                .totalAssistanceCount(h.getTotalAssistanceCount())
                .agapeAmount(h.getAgapeAmount())
                .totalSolidarityCollected(h.getTotalSolidarityCollected())
                .totalSolidarityCount(h.getTotalSolidarityCount())
                .totalEpargneDeposited(h.getTotalEpargneDeposited())
                .totalEpargneWithdrawn(h.getTotalEpargneWithdrawn())
                .totalEmpruntAmount(h.getTotalEmpruntAmount())
                .totalRemboursementAmount(h.getTotalRemboursementAmount())
                .totalInteretAmount(h.getTotalInteretAmount())
                .totalRenfoulementCollected(h.getTotalRenfoulementCollected())
                .totalRegistrationCollected(h.getTotalRegistrationCollected())
                .mutuelleCash(h.getMutuelleCash())
                .mutuellesSavingAmount(h.getMutuellesSavingAmount())
                .mutuelleSolidarityAmount(h.getMutuelleSolidarityAmount())
                .mutuelleRegistrationAmount(h.getMutuelleRegistrationAmount())
                .mutuelleBorrowAmount(h.getMutuelleBorrowAmount())
                .totalTransactions(h.getTotalTransactions())
                .activeMembersCount(h.getActiveMembersCount())
                .createdAt(h.getCreatedAt())
                .build();
    }

    public ExerciceHistoryDto toDTO(ExerciceHistory h) {
        return ExerciceHistoryDto.builder()
                .id(h.getId())
                .exerciceId(h.getExercice().getId())
                .exerciceName(h.getExercice().getName())
                .exerciceStartDate(h.getExercice().getStartDate())
                .exerciceEndDate(h.getExercice().getEndDate())
                .exerciceStatus(h.getExercice().getStatus())
                .totalAssistanceAmount(h.getTotalAssistanceAmount())
                .totalAssistanceCount(h.getTotalAssistanceCount())
                .totalAgapeAmount(h.getTotalAgapeAmount())
                .totalSolidarityCollected(h.getTotalSolidarityCollected())
                .totalEpargneDeposited(h.getTotalEpargneDeposited())
                .totalEpargneWithdrawn(h.getTotalEpargneWithdrawn())
                .totalEmpruntAmount(h.getTotalEmpruntAmount())
                .totalRemboursementAmount(h.getTotalRemboursementAmount())
                .totalInteretAmount(h.getTotalInteretAmount())
                .totalRenfoulementDistributed(h.getTotalRenfoulementDistributed())
                .renfoulementUnitAmount(h.getRenfoulementUnitAmount())
                .totalRenfoulementCollected(h.getTotalRenfoulementCollected())
                .totalRegistrationCollected(h.getTotalRegistrationCollected())
                .mutuelleCash(h.getMutuelleCash())
                .mutuellesSavingAmount(h.getMutuellesSavingAmount())
                .mutuelleSolidarityAmount(h.getMutuelleSolidarityAmount())
                .mutuelleRegistrationAmount(h.getMutuelleRegistrationAmount())
                .mutuelleBorrowAmount(h.getMutuelleBorrowAmount())
                .totalTransactions(h.getTotalTransactions())
                .sessionsCount(h.getSessionsCount())
                .activeMembersCount(h.getActiveMembersCount())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers privés
    // ─────────────────────────────────────────────────────────────────────────

    private BigDecimal sum(Long sessionId, Long accountMemberId, TransactionType type, TransactionDirection direction) {
        return transactionRepository.sumBySessionAndAccountMemberAndTypeAndDirection(sessionId, accountMemberId, type, direction);
    }

    private BigDecimal sumEx(Long exerciceId, Long accountMemberId, TransactionType type, TransactionDirection direction) {
        return transactionRepository.sumByExerciceAndAccountMemberAndTypeAndDirection(exerciceId, accountMemberId, type, direction);
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
