package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.account.AccountMemberDTO;
import com.mutuelle.mobille.dto.account.AccountMemberFullDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.service.BorrowingCeilingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountMemberMapper {

    private final BorrowingCeilingService borrowingCeilingService;
    private final MemberMapper memberMapper;

    public AccountMemberDTO toAccountMemberDto(AccountMember account) {
        if (account == null) return null;
        BigDecimal plafond=borrowingCeilingService.calculerPlafond(account.getSavingAmount());

        return AccountMemberDTO.builder()
                .id(account.getId())
                .savingAmount(account.getSavingAmount())
                .unpaidRegistrationAmount(account.getUnpaidRegistrationAmount())
                .baseRegistrationAmount(account.getBaseRegistrationAmount())
                .solidarityAmount(account.getSolidarityAmount())
                .unpaidSolidarityAmount(account.getUnpaidSolidarityAmount())
                .memberId(account.getMember().getId())
                .borrowAmount(account.getBorrowAmount())
                .lastInterestDate(account.getLastInterestDate())
                .unpaidRenfoulement(account.getUnpaidRenfoulement())
                .isActive(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .maxBorrow(plafond)
                .build();
    }

    public AccountMemberFullDTO toFullDto(AccountMember accountMember) {
        if (accountMember == null) {
            return null;
        }

        BigDecimal plafond = borrowingCeilingService.calculerPlafond(accountMember.getSavingAmount());

        MemberProfileDTO memberDto = memberMapper.toProfileDTO(accountMember.getMember());
        // Si vous avez déjà toProfileDTO → vous pouvez aussi faire :
        // MemberProfileDTO profile = memberMapper.toProfileDTO(accountMember.getMember());
        // puis convertir vers MemberResponseDTO si besoin

        return AccountMemberFullDTO.builder()
                .id(accountMember.getId())
                .memberId(accountMember.getMember().getId())
                .savingAmount(accountMember.getSavingAmount())
                .unpaidRegistrationAmount(accountMember.getUnpaidRegistrationAmount())
                .solidarityAmount(accountMember.getSolidarityAmount())
                .unpaidSolidarityAmount(accountMember.getUnpaidSolidarityAmount())
                .borrowAmount(accountMember.getBorrowAmount())
                .unpaidRenfoulement(accountMember.getUnpaidRenfoulement())
                .lastInterestDate(accountMember.getLastInterestDate())
                .maxBorrow(plafond)
                .isActive(accountMember.isActive())
                .createdAt(accountMember.getCreatedAt())
                .updatedAt(accountMember.getUpdatedAt())
                .profile(memberDto)
                .build();
    }
}

