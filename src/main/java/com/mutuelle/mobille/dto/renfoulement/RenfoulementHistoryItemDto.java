package com.mutuelle.mobille.dto.renfoulement;

import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceHistoryDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.dto.profile.AdminProfileDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import com.mutuelle.mobille.models.ExerciceHistory;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RenfoulementHistoryItemDto {
    private Long id;
    private String exerciceName;
    private LocalDateTime calculatedAt;
    private BigDecimal unitAmount;
    // Pour admin : ajouter stats globaux
    private int baseMembersCount;
    private int distributedMembersCount;
    private BigDecimal totalToDistributeAmount;
    private BigDecimal expectedTotalAmount;
    private ExerciceHistoryDto exerciceHistory;
}