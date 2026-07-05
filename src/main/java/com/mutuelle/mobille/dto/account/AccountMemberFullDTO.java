package com.mutuelle.mobille.dto.account;

import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMemberFullDTO {
    private Long id;
    private Long memberId;
    private BigDecimal savingAmount;
    private BigDecimal unpaidRegistrationAmount;
    private BigDecimal solidarityAmount;
    private BigDecimal borrowAmount;
    private BigDecimal unpaidRenfoulement;
    private LocalDateTime lastInterestDate;
    private BigDecimal unpaidSolidarityAmount ;
    private BigDecimal maxBorrow;
    private boolean isActive;
    private MemberProfileDTO profile;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}