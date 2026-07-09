package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.MemberStatus;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.AccountMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MemberComplianceService {

    public static final int LOAN_BLOCK_SESSION_THRESHOLD = 3;
    public static final int LATE_RENF_ASSISTANCE_COOLDOWN = 3;

    private final MutuelleConfigService mutuelleConfigService;
    private final AccountMemberRepository accountMemberRepository;

    public boolean isRegistrationConfigured(AccountMember account) {
        return account != null && account.isRegistrationConfigured();
    }

    /**
     * Membre à jour pour le calcul du renflouement : inscrit, solidarité et renflouement soldés.
     */
    public boolean isMemberAJourForRenfoulement(AccountMember account) {
        if (account == null || account.getMember() == null) {
            return false;
        }
        if (account.getMember().getStatus() == MemberStatus.PENDING) {
            return false;
        }
        return nz(account.getUnpaidRegistrationAmount()).compareTo(BigDecimal.ZERO) == 0
                && nz(account.getUnpaidSolidarityAmount()).compareTo(BigDecimal.ZERO) == 0
                && nz(account.getUnpaidRenfoulement()).compareTo(BigDecimal.ZERO) == 0;
    }

    /** Membre interne à la réunion (hors non-inscrits). */
    public boolean isEligibleForRenfoulementAssignment(Member member) {
        return member != null && member.getStatus() != MemberStatus.PENDING;
    }

    public boolean hasComplianceDebt(AccountMember account) {
        if (account == null) return false;
        BigDecimal solidarity = nz(account.getUnpaidSolidarityAmount());
        BigDecimal renfoulement = nz(account.getUnpaidRenfoulement());
        return solidarity.compareTo(BigDecimal.ZERO) > 0 || renfoulement.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean computeInsolvable(AccountMember account) {
        if (account == null || !hasComplianceDebt(account)) {
            return false;
        }
        return account.getSessionsInNonAJour() >= LOAN_BLOCK_SESSION_THRESHOLD;
    }

    public int computeSessionsBeforeLoanBlock(AccountMember account) {
        if (account == null || !hasComplianceDebt(account)) {
            return LOAN_BLOCK_SESSION_THRESHOLD;
        }
        int remaining = LOAN_BLOCK_SESSION_THRESHOLD - account.getSessionsInNonAJour();
        return Math.max(remaining, 0);
    }

    public boolean computeAssistanceBlocked(AccountMember account) {
        return account != null && account.getAssistanceBlockedSessionsRemaining() > 0;
    }

    public String computeStatusLabel(com.mutuelle.mobille.enums.MemberStatus status) {
        if (status == null) return "Inconnu";
        return switch (status) {
            case PENDING -> "Non inscrit";
            case ACTIF -> "À jour";
            case NON_A_JOUR -> "Non à jour";
            case INACTIF -> "Inactif";
        };
    }

    @Transactional
    public void onSessionClosed(Session session) {
        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        BigDecimal threshold = config.getInsolvencyThreshold();

        for (AccountMember account : accountMemberRepository.findAll()) {
            if (!account.isActive()) continue;

            if (account.getAssistanceBlockedSessionsRemaining() > 0) {
                account.setAssistanceBlockedSessionsRemaining(
                        account.getAssistanceBlockedSessionsRemaining() - 1);
            }

            BigDecimal solidarity = nz(account.getUnpaidSolidarityAmount());
            BigDecimal renfoulement = nz(account.getUnpaidRenfoulement());
            BigDecimal totalDebt = solidarity.add(renfoulement);

            if (totalDebt.compareTo(BigDecimal.ZERO) > 0
                    && totalDebt.compareTo(threshold) < 0) {
                account.setSessionsInNonAJour(account.getSessionsInNonAJour() + 1);
            } else if (totalDebt.compareTo(BigDecimal.ZERO) == 0) {
                account.setSessionsInNonAJour(0);
            }

            accountMemberRepository.save(account);
        }
    }

    @Transactional
    public void onDebtCleared(AccountMember account) {
        if (account == null) return;
        if (!hasComplianceDebt(account)) {
            account.setSessionsInNonAJour(0);
            accountMemberRepository.save(account);
        }
    }

    @Transactional
    public void onLateRenfoulementPaid(AccountMember account) {
        if (account == null) return;
        if (account.getSessionsInNonAJour() >= 4) {
            account.setAssistanceBlockedSessionsRemaining(LATE_RENF_ASSISTANCE_COOLDOWN);
            accountMemberRepository.save(account);
        }
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
