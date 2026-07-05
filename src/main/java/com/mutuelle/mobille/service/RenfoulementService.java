package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.renfoulement.RenfoulementHistoryItemDto;
import com.mutuelle.mobille.dto.renfoulement.RenfoulementHistoryResponseDto;
import com.mutuelle.mobille.dto.renfoulement.RenfoulementSimulationDto;
import com.mutuelle.mobille.mapper.AccountMemberMapper;
import com.mutuelle.mobille.mapper.ExerciceHistoryMapper;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenfoulementService {

    private final MemberRepository memberRepository;
    private final RenfoulementRepository renfoulementRepository;
    private final SessionHistoryRepository sessionHistoryRepository;
    private final ExerciceService exerciceService;
    private final AccountMemberRepository accountMemberRepository;
    private final AccountMemberMapper accountMemberMapper;


    public RenfoulementHistoryResponseDto getGlobalRenfoulementHistory( ) {

        // 2. Récupérer tous les renfoulements VALIDÉS des exercices précédents
        List<Renfoulement> pastRenfoulements = renfoulementRepository.findAll();

        List<RenfoulementHistoryItemDto> historyItems = pastRenfoulements.stream()
                .map(this::toGlobalHistoryItem)
                .collect(Collectors.toList());

        // 3. Simulation pour l'exercice courant
        RenfoulementSimulationDto simulation = simulateCurrentRenfoulement( );

        List<AccountMember> accountMembers=accountMemberRepository.findByUnpaidRenfoulementGreaterThan(BigDecimal.ZERO);

        return RenfoulementHistoryResponseDto.builder()
                .pastRenfoulements(historyItems)
                .accountMembers( accountMembers.stream().map(accountMemberMapper::toFullDto).toList())
                .currentSimulation(simulation)
                .build();
    }

    private RenfoulementSimulationDto simulateCurrentRenfoulement( ) {
        //   Trouver l'exercice courant (celui qui n'est pas encore terminé)
        Optional<ExerciceResponseDTO> exerciceOpt = exerciceService.getCurrentExerciceDTO();
        if(exerciceOpt.isEmpty()){
            return RenfoulementSimulationDto.builder()
                    .exerciceName("")
                    .isPossible(false)
                    .message("Aucun exercice en cours trouvé")
                    .build();
        }

        ExerciceResponseDTO exercice=exerciceOpt.get();

        // 1. Calcul du total à répartir
        List<SessionHistory> historyList = sessionHistoryRepository.findAllByExerciceId(exercice.getId());

        BigDecimal totalADistribuer = historyList.stream()
                .map(s -> s.getTotalAssistanceAmount().add(s.getAgapeAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalADistribuer.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Aucun renfoulement à calculer pour l'exercice {} (total = 0)", exercice.getId());
            return RenfoulementSimulationDto.builder()
                    .exerciceName(exercice.getName())
                    .isPossible(false)
                    .message("Aucun montant à répartir (pas de dépenses/assistance/agape)")
                    .build();
        }

        // 2. Membres à jour (ceux qui servent de base au calcul du montant unitaire)
        List<AccountMember> comptesAJour = memberRepository.findAllActiveWithAccount().stream()
                .map(Member::getAccountMember)
                .filter(compte -> compte != null
                        && compte.getUnpaidRegistrationAmount().compareTo(BigDecimal.ZERO) == 0
                        && compte.getUnpaidSolidarityAmount().compareTo(BigDecimal.ZERO) == 0)
                .toList();

        int nbMembresAJour = comptesAJour.size();
        if (nbMembresAJour == 0) {
            log.warn("Aucun membre à jour → impossible de calculer le renfoulement pour l'exercice {}", exercice.getId());
            return RenfoulementSimulationDto.builder()
                    .exerciceName(exercice.getName())
                    .isPossible(false)
                    .message("Auccun Membre a jours")
                    .build();
        }

        // 3. Montant unitaire de renfoulement (arrondi commercial vers le bas à la tranche de 25)
        BigDecimal renfoulementUnitaire = MoneyUtil.floorToNearest25(
                totalADistribuer.divide(
                        BigDecimal.valueOf(nbMembresAJour),
                        2,
                        RoundingMode.HALF_UP
                )
        );

        if (renfoulementUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            return RenfoulementSimulationDto.builder()
                    .exerciceName(exercice.getName())
                    .message("Renfoulement incoherant")
                    .isPossible(false)
                    .build();
        }

        // 4. Tous les membres actifs (ceux qui vont recevoir la dette)
        List<Member> membresActifs = memberRepository.findByIsActiveTrue();

        if (membresActifs.isEmpty()) {
            log.info("Aucun membre actif pour assigner le renfoulement");
            return RenfoulementSimulationDto.builder()
                    .exerciceName(exercice.getName())
                    .isPossible(false)
                    .message("Aucun membre actif dans l'association")
                    .build();
        }

        return  RenfoulementSimulationDto.builder()
                .estimatedExpectedTotalAmount(totalADistribuer)
                .estimatedBaseMembersCount(nbMembresAJour)
                .estimatedUnitAmount(renfoulementUnitaire)
                .estimatedDistributedMembersCount(membresActifs.size())
                .estimatedTotalToDistributeAmount(totalADistribuer)
                .exerciceName(exercice.getName())
                .isPossible(true)
                .build();

    }

    private RenfoulementHistoryItemDto toGlobalHistoryItem(Renfoulement r) {

        Exercice ex = r.getExercice();

        return RenfoulementHistoryItemDto.builder()
                .id(r.getId())
                .exerciceName(r.getExercice().getName())
                .calculatedAt(r.getCreatedAt())
                .unitAmount(r.getUnitAmount())
                .baseMembersCount(r.getBaseMembersCount())
                .distributedMembersCount(r.getDistributedMembersCount())
                .totalToDistributeAmount(r.getTotalToDistributeAmount())
                .expectedTotalAmount(r.getExpectedTotalAmount())
                .exerciceHistory(ExerciceHistoryMapper.toDto(ex != null ? ex.getHistory() : null))
                .build();
    }
}