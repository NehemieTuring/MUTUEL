package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.assistance.*;
import com.mutuelle.mobille.enums.AssistanceStatus;
import com.mutuelle.mobille.enums.StatusSession;
import com.mutuelle.mobille.enums.TransactionDirection;
import com.mutuelle.mobille.enums.TransactionType;
import com.mutuelle.mobille.mapper.AssistanceMapper;
import com.mutuelle.mobille.models.*;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.repository.*;
import com.mutuelle.mobille.service.notifications.SessionNotificationHelper;
import com.mutuelle.mobille.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    // Créer une assistance en PENDING (pas de débit, pas de transaction)
    public AssistanceResponseDto createAssistance(CreateAssistanceDto dto) {
        // Récupérer le membre : soit depuis dto.memberId() si ADMIN, soit depuis SecurityUtil si MEMBER
        final Long memberId = dto.memberId() != null ? dto.memberId() : SecurityUtil.getCurrentUserRefId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Membre non trouvé : " + memberId));

        // Vérifier que le membre est en règle (inscription + solidarité + renflouement)
        var account = member.getAccountMember();
        if (account != null) {
            java.math.BigDecimal zero = java.math.BigDecimal.ZERO;

            if (account.getUnpaidRegistrationAmount() != null &&
                account.getUnpaidRegistrationAmount().compareTo(zero) > 0) {
                throw new IllegalStateException(
                    "Demande refusée : ce membre n'a pas encore payé ses frais d'inscription."
                );
            }
            if (account.getUnpaidSolidarityAmount() != null &&
                account.getUnpaidSolidarityAmount().compareTo(zero) > 0) {
                throw new IllegalStateException(
                    "Demande refusée : ce membre a une cotisation de solidarité impayée."
                );
            }
            if (account.getUnpaidRenfoulement() != null &&
                account.getUnpaidRenfoulement().compareTo(zero) > 0) {
                throw new IllegalStateException(
                    "Demande refusée : ce membre a un renflouement impayé."
                );
            }
        }

        TypeAssistance typeAssistance = typeAssistanceRepository.findById(dto.typeAssistanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type d'assistance non trouvé : " + dto.typeAssistanceId()));

        // Session : soit depuis dto.sessionId(), soit la session courante
        Session session;
        if (dto.sessionId() != null) {
            session = sessionRepository.findById(dto.sessionId())
                    .orElseThrow(() -> new EntityNotFoundException("Session non trouvée : " + dto.sessionId()));
        } else {
            session = sessionRepository.findAll().stream()
                    .filter(s -> s.getStatus() == StatusSession.IN_PROGRESS)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Aucune session active pour soumettre une demande d'assistance"));
        }

        // Créer la demande en PENDING (pas de débit, pas de transaction)
        Assistance assistance = Assistance.builder()
                .description(dto.description())
                .typeAssistance(typeAssistance)
                .amountMove(typeAssistance.getAmount())
                .member(member)
                .session(session)
                .status(AssistanceStatus.PENDING)
                .rejectReason(null)
                .transaction(null)
                .build();

        Assistance saved = assistanceRepository.save(assistance);
        notificationHelper.notifyAssistanceCreated(saved);
        return assistanceMapper.toResponseDto(saved);
    }

    // Approuver ou rejeter une demande d'assistance
    public AssistanceResponseDto updateAssistanceStatus(Long id, UpdateAssistanceStatusDto dto) {
        Assistance assistance = assistanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assistance non trouvée : " + id));

        if (assistance.getStatus() != AssistanceStatus.PENDING) {
            throw new IllegalStateException("Cette demande a déjà été traitée (statut : " + assistance.getStatus() + ")");
        }

        if (dto.status() == AssistanceStatus.APPROVED) {
            // Vérifier et débiter la caisse solidarité
            AccountMutuelle globalAccount = accountService.getMutuelleGlobalAccount();
            BigDecimal requiredAmount = assistance.getAmountMove();

            if (globalAccount.getSolidarityAmount().compareTo(requiredAmount) < 0) {
                throw new IllegalStateException("Fonds insuffisants. Disponible : "
                        + globalAccount.getSolidarityAmount() + ", requis : " + requiredAmount);
            }

            globalAccount.setSolidarityAmount(globalAccount.getSolidarityAmount().subtract(requiredAmount));
            accountMutuelleRepository.save(globalAccount);

            // Créer la transaction
            Transaction transaction = Transaction.builder()
                    .transactionType(TransactionType.ASSISTANCE)
                    .transactionDirection(TransactionDirection.DEBIT)
                    .amount(requiredAmount)
                    .description("Assistance approuvée : " + assistance.getTypeAssistance().getName())
                    .accountMember(assistance.getMember().getAccountMember())
                    .session(assistance.getSession())
                    .build();

            Transaction savedTx = transactionRepository.save(transaction);
            assistance.setTransaction(savedTx);
            assistance.setStatus(AssistanceStatus.APPROVED);
            assistance.setRejectReason(null);

        } else if (dto.status() == AssistanceStatus.REJECTED) {
            assistance.setStatus(AssistanceStatus.REJECTED);
            assistance.setRejectReason(dto.rejectReason());
            // Pas de débit, pas de transaction

        } else {
            throw new IllegalArgumentException("Statut invalide : " + dto.status());
        }

        Assistance updated = assistanceRepository.save(assistance);
        return assistanceMapper.toResponseDto(updated);
    }

    // Récupérer les assistances du membre connecté
    @Transactional(readOnly = true)
    public List<AssistanceResponseDto> getMyAssistances() {
        Long memberId = SecurityUtil.getCurrentUserRefId();
        return assistanceRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(assistanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le montant total des assistances validées/accordées pour une session donnée.
     */
    public BigDecimal getTotalAssistanceAmountForSession(Long sessionId) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée : " + sessionId));

        return transactionRepository.sumAssistanceAmountBySessionId(sessionId);
    }

    public Long countTotalAssistanceForSession(Long sessionId) {
        return assistanceRepository.countBySessionId(sessionId);
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

        return assistances.map(a -> assistanceMapper.toResponseDto(a));
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
