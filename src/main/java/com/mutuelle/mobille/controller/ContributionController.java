package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.contribution.ContributionPaymentRequestDto;
import com.mutuelle.mobille.dto.contribution.ContributionPaymentResponseDto;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.ContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
@Tag(name = "Contributions", description = "Gestion des paiements d'agapes et d'inscription")
public class ContributionController {

    private final ContributionService contributionService;
    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;

    // ========================================================================================
    // PAYER  L'INSCRIPTION (partiellement ou totalement)
    // ========================================================================================
    @PostMapping("/pay")
    @Operation(summary = "Payer une contribution ( inscription ou renfoulement )")
    public ResponseEntity<ApiResponseDto<ContributionPaymentResponseDto>> payContribution(
            @Valid @RequestBody ContributionPaymentRequestDto request
    ) {
        ContributionPaymentResponseDto response = contributionService.processContributionPayment(request);

        return ResponseEntity.ok(ApiResponseDto.ok(response, response.message()));
    }
}