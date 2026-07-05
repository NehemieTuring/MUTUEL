package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.assistance.*;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.enums.TemplateMailsName;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.mapper.AssistanceMapper;
import com.mutuelle.mobille.mapper.TransactionMapper;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.service.notifications.SessionNotificationHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssistanceService {

    private final TypeAssistanceRepository typeAssistanceRepository;
    private final AssistanceRepository assistanceRepository;
    private final SessionRepository sessionRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final AccountMutuelleRepository accountMutuelleRepository;
    private final AssistanceMapper assistanceMapper;
    private final SessionNotificationHelper notificationHelper;

    // Récupérer tous les types d'assistance
    @Transactional(readOnly = true)
    public List<TypeAssistanceResponseDto> getAllTypeAssistances() {
        return typeAssistanceRepository.findAll().stream()
                .map(this::mapToTypeAssistanceResponseDto)
                .collect(Collectors.toList());
    }

    // Créer un nouveau type d'assistance
    public TypeAssistanceResponseDto createTypeAssistance(CreateTypeAssistanceDto dto) {
        TypeAssistance type = TypeAssistance.builder()
                .name(dto.name())
                .description(dto.description())
                .amount(dto.amount())
                .build();

        TypeAssistance saved = typeAssistanceRepository.save(type);
        return mapToTypeAssistanceResponseDto(saved);
    }

    // Mettre à jour un type d'assistance
    public TypeAssistanceResponseDto updateTypeAssistance(Long id, UpdateTypeAssistanceDto dto) {
        TypeAssistance type = typeAssistanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Type d'assistance non trouvé avec l'ID : " + id));

        type.setName(dto.name());
        type.setDescription(dto.description());
        type.setAmount(dto.amount());

        TypeAssistance updated = typeAssistanceRepository.save(type);
        return mapToTypeAssistanceResponseDto(updated);
    }

    // Créer une assistance
    public AssistanceResponseDto createAssistance(CreateAssistanceDto dto) {
        // Récupération des entités nécessaires
        AccountMutuelle globalAccount = accountService.getMutuelleGlobalAccount();

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new EntityNotFoundException("Membre non trouvé avec l'ID : " + dto.memberId()));

        TypeAssistance typeAssistance = typeAssistanceRepository.findById(dto.typeAssistanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type d'assistance non trouvé avec l'ID : " + dto.typeAssistanceId()));

        Session session = sessionRepository.findById(dto.sessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'ID : " + dto.sessionId()));

        //  La session doit être EN COURS
        if (session.getStatus() != StatusSession.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format("Impossible de payer la solidarité : la session '%s' n'est pas en cours. (Statut actuel: %s)",
                            session.getName(),
                            session.getStatus()
                    )
            );
        }

        BigDecimal requiredAmount = typeAssistance.getAmount();

        // Vérification du solde de solidarité du compte global
        if (globalAccount.getSolidarityAmount().compareTo(requiredAmount) < 0) {
            throw new IllegalStateException("La mutuelle ne dispose pas d'assez de fonds pour cette assistance. Solde disponible : "
                    + globalAccount.getSolidarityAmount() + ", requis : " + requiredAmount);
        }

        // Débit du compte global de la mutuelle
        globalAccount.setSolidarityAmount(globalAccount.getSolidarityAmount().subtract(requiredAmount));
        // Pas besoin de save ici si AccountMutuelle est géré par cascade ou si le service le fait, sinon ajouter un save si nécessaire
        accountMutuelleRepository.save(globalAccount);

        // Créer la transaction associée (type ASSISTANCE)
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.ASSISTANCE)
                .transactionDirection(TransactionDirection.DEBIT)
                .amount(requiredAmount)
                .description("Demande d'assistance : " + typeAssistance.getName())
                .accountMember(member.getAccountMember())
                .session(session)
                .build();


        Transaction savedTransaction = transactionRepository.save(transaction);


        // Créer l'assistance
        Assistance assistance = Assistance.builder()
                .description(dto.description())
                .typeAssistance(typeAssistance)
                .transaction(savedTransaction)
                .amountMove(requiredAmount)
                .member(member)
                .session(session)
                .build();
        Assistance savedAssistance = assistanceRepository.save(assistance);

        //
        notificationHelper.notifyAssistanceCreated(savedAssistance);

        return assistanceMapper.toResponseDto(savedAssistance);
    }


    /**
     * Calcule le montant total des assistances validées/accordées pour une session donnée.
     * (on peut filtrer sur un statut si tu en ajoutes un plus tard : APPROVED, PAID, etc.)
     */
    public BigDecimal getTotalAssistanceAmountForSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée : " + sessionId));

        return transactionRepository.sumAssistanceAmountBySessionId(sessionId);
    }

    public Long countTotalAssistanceForSession(Long sessionId){
        return  assistanceRepository.countBySessionId(sessionId);
    }

    // Nombre total d'assistances pour un membre donné
    public Long countAssistancesByMember(Long memberId) {
        return assistanceRepository.countByMemberId(memberId);
    }

    // Nombre total d'assistances global (toutes sessions, tous membres)
    public Long countAllAssistances() {
        return assistanceRepository.count();
    }

    // Somme totale des montants d'assistances global
    public BigDecimal sumAllAssistanceAmounts() {
        return transactionRepository.sumAmountByType(TransactionType.ASSISTANCE);
    }

    public Page<AssistanceResponseDto> getAssistancesFiltered(
            Long typeAssistanceId,
            Long memberId,
            Long sessionId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        LocalDateTime debut = fromDate != null ? fromDate : LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime fin   = toDate   != null ? toDate   : LocalDateTime.now().plusYears(10);

        Page<Assistance> assistances = assistanceRepository.findAllFiltered(
                typeAssistanceId,
                memberId,
                sessionId,
                debut,
                fin,
                pageable
        );

        return  assistances.map(a->assistanceMapper.toResponseDto(a));
    }

    private TypeAssistanceResponseDto mapToTypeAssistanceResponseDto(TypeAssistance type) {
        return new TypeAssistanceResponseDto(
                type.getId(),
                type.getName(),
                type.getAmount(),
                type.getDescription()
        );
    }


}