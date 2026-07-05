package com.mutuelle.mobille.mapper;

import com.mutuelle.mobille.dto.account.AccountMemberDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.models.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberProfileDTO toProfileDTO(Member member) {
        if (member == null) return null;

        return MemberProfileDTO.builder()
                .id(member.getId())
                .firstname(member.getFirstname())
                .lastname(member.getLastname())
                .phone(member.getPhone())
                .avatar(member.getAvatar())
                .isActive(member.isActive())
                .pin(member.getPin())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .account(toAccountDTO(member.getAccountMember()))
                .build();
    }

    private AccountMemberDTO toAccountDTO(com.mutuelle.mobille.models.account.AccountMember account) {
        if (account == null) return null;

        return AccountMemberDTO.builder()
                .id(account.getId())
                .savingAmount(account.getSavingAmount())
                .unpaidRegistrationAmount(account.getUnpaidRegistrationAmount())
                .solidarityAmount(account.getSolidarityAmount())
                .borrowAmount(account.getBorrowAmount())
                .unpaidRenfoulement(account.getUnpaidRenfoulement())
                .isActive(account.isActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}