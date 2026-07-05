package com.mutuelle.mobille.service;

import com.mutuelle.mobille.config.security.CustomUserDetails;
import com.mutuelle.mobille.dto.CurrentContextResponseDTO;
import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.dto.profile.AdminProfileDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.mapper.AdminMapper;
import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import com.mutuelle.mobille.models.Member;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.Session;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.repository.AdminRepository;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.TransactionRepository;
import com.mutuelle.mobille.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrentContextService {

    private final SessionService sessionService;
    private final ExerciceService exerciceService;
    private final AssistanceService assistanceService;
    private final MutuelleConfigService mutuelleConfigService;
    private final BorrowingCeilingService borrowingCeilingService;
    private final MemberService memberService;
    private final AdminMapper adminMapper;
    private final AdminRepository adminRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final MemberRepository memberRepository;

    public CurrentContextResponseDTO buildCurrentContext() {

        // ───────────────────── Détection + récupération utilisateur ─────────────────────
        boolean authenticated = false;
        CustomUserDetails userDetails = null;
        try {
            userDetails = SecurityUtil.getCurrentCustomUserDetails();
            authenticated = true;
        } catch (IllegalStateException e) {
            // Pas connecté ou principal non compatible → on reste anonyme
            authenticated = false;
        }

        // Builder de base
        CurrentContextResponseDTO.CurrentContextResponseDTOBuilder builder = CurrentContextResponseDTO.builder()
                .authenticated(authenticated);

        // ───────────────────── Enrichissement si connecté ─────────────────────
        if (authenticated && userDetails != null) {
            AuthUser authUser = userDetails.getAuthUser();
            Long userRefId = authUser.getUserRefId();

            builder
                    .email(userDetails.getEmail())
                    .authUserId(userDetails.getId())
                    .role(authUser.getRole())
                    .userRefId(userRefId)
                    .isAdmin(authUser.getRole() == Role.ADMIN)
                    .isMember(authUser.getRole() == Role.MEMBER);

            // Profil
            if (authUser.getRole() == Role.ADMIN) {
                adminRepository.findById(userRefId)
                        .ifPresent(a -> builder.adminProfile(adminMapper.toProfileDTO(a)));
                builder.accountMutuelle(accountService.getMutuelleGlobalAccount());
            } else if (authUser.getRole() == Role.MEMBER) {
                MemberResponseDTO memberResponseDTO= memberService.getCurrentMemberProfile();
                builder.memberProfile(memberResponseDTO);

                // Statistiques MEMBRE
                Long memberId = memberResponseDTO.id();
                Long accountMemberId = memberResponseDTO.idAccount();

                builder
                        .memberTotalAssistances(assistanceService.countAssistancesByMember(memberId))
                        .memberTotalTransactions(transactionRepository.countByAccountMemberId(accountMemberId));
            }

            // Statistiques ADMIN (globales)
            if (authUser.getRole() == Role.ADMIN) {
                builder
                        .globalTotalAssistances(assistanceService.countAllAssistances())
                        .globalTotalTransactions(transactionRepository.countTotalTransactions())
                        .globalTotalAssistanceAmount(assistanceService.sumAllAssistanceAmounts());

                // Optionnel : stats session courante

            }
        }

        // ───────────────────── Données de base (comme avant) ─────────────────────
        Optional<Session> optionalSession = sessionService.findCurrentSession();

        SessionResponseDTO currentSessionDto = optionalSession
                .map(sessionService::toResponseDTO)
                .orElse(null);

        ExerciceResponseDTO currentExerciceDto = optionalSession
                .map(Session::getExercice)
                .map(exerciceService::toResponseDTO)
                .orElseGet(() -> exerciceService.findCurrentExercice()
                        .map(exerciceService::toResponseDTO)
                        .orElse(null));

        MutuelleConfig config = mutuelleConfigService.getCurrentConfig();
        List<TypeAssistanceResponseDto> typeAssistance = assistanceService.getAllTypeAssistances();
        List<BorrowingCeilingInterval> borrowingCeilingIntervals = borrowingCeilingService.getAllIntervalsOrdered();
        Long totalMembers = memberRepository.count();

        // Finalisation
        return builder
                .currentSession(currentSessionDto)
                .currentExercice(currentExerciceDto)
                .config(config)
                .typeAssistance(typeAssistance)
                .borrowingCeilingIntervals(borrowingCeilingIntervals)
                .totalMembers(totalMembers)
                .build();
    }

}