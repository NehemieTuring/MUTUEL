package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.account.AccountMemberDTO;
import com.mutuelle.mobille.mapper.AccountMemberMapper;
import com.mutuelle.mobille.models.account.AccountMember;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import com.mutuelle.mobille.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Comptes (Membres et Mutuelle)", description = "Gestion de la consultation des comptes membres et caisse mutuelle")
public class AccountController {

    private final AccountService accountService;
    private final AccountMemberMapper accountMemberMapper;

    // ───────────────────────────────────────────────────────────────
    // Comptes des membres
    // ───────────────────────────────────────────────────────────────

    @GetMapping("/members")
    @Operation(summary = "Lister tous les comptes des membres")
    public ResponseEntity<ApiResponseDto<List<AccountMemberDTO>>> getAllMemberAccounts() {
        List<AccountMember> accounts = accountService.getAllMemberAccounts();

        List<AccountMemberDTO> dtos = accounts.stream()
                .map(accountMemberMapper::toAccountMemberDto)
                .toList();

        return ResponseEntity.ok(
                ApiResponseDto.ok(dtos, "Liste de tous les comptes membres récupérée")
        );
    }

    @GetMapping("/members/{id}")
    @Operation(summary = "Récupérer le compte d'un membre spécifique par son ID")
    public ResponseEntity<ApiResponseDto<AccountMemberDTO>> getMemberAccountById(
            @PathVariable Long id
    ) {
        AccountMemberDTO accountDto =
                accountMemberMapper.toAccountMemberDto(
                        accountService.getMemberAccountById(id)
                );

        return ResponseEntity.ok(
                ApiResponseDto.ok(accountDto, "Compte du membre récupéré avec succès")
        );
    }

    @GetMapping("/members/by-member/{memberId}")
    @Operation(summary = "Récupérer le compte d'un membre à partir de son ID membre")
    public ResponseEntity<ApiResponseDto<AccountMemberDTO>> getMemberAccountByMemberId(@PathVariable Long memberId) {
        AccountMemberDTO accountDto =
                accountMemberMapper.toAccountMemberDto(
                        accountService.getMemberAccountByMemberId(memberId)
                );
        return ResponseEntity.ok(
                ApiResponseDto.ok(accountDto, "Compte du membre récupéré avec succès")
        );
    }

    // ───────────────────────────────────────────────────────────────
    // Compte Mutuelle (caisse centrale)
    // ───────────────────────────────────────────────────────────────

    @GetMapping("/mutuelle")
    @Operation(summary = "Récupérer l'état de la caisse mutuelle (compte central)")
    public ResponseEntity<ApiResponseDto<AccountMutuelle>> getMutuelleAccount() {
        AccountMutuelle account = accountService.getMutuelleGlobalAccount();
        return ResponseEntity.ok(
                ApiResponseDto.ok(account, "État de la caisse mutuelle récupéré")
        );
    }
}