package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.ApiResponseDto;
import com.mutuelle.mobille.dto.epargne.EpargneRequestDto;
import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.models.Transaction;
import com.mutuelle.mobille.repository.MemberRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import com.mutuelle.mobille.service.EpargneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mutuelle.mobille.service.SessionService;

import java.util.Optional;

@RestController
@RequestMapping("/api/epargne")
@RequiredArgsConstructor
@Tag(name = "Épargne", description = "Gestion des transactions d'épargne")
public class EpargneController {

    private final EpargneService epargneService;
    private final MemberRepository memberRepository;

    // ========================================================================================
    // 1) CREER UNE TRANSACTION D'EPARGNE
    // ========================================================================================

    @PostMapping
    @Operation( summary = "Faire une épargne ou retirer",
            description = "Permet de créer une transaction d'épargne pour un membre dans une session donnée.")
    public ResponseEntity<ApiResponseDto<TransactionResponseDTO>> epargne(@Valid @RequestBody EpargneRequestDto requestDto) {
        // 1. Vérification existence membre
        if (!memberRepository.existsById(requestDto.getMemberId())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error("Le membre avec l'id " + requestDto.getMemberId() + " n'existe pas")
            );
        }

//        // 2. Vérification existence session
//        if (!sessionRepository.existsById(requestDto.getSessionId())) {
//            return ResponseEntity.badRequest().body(
//                    ApiResponseDto.error("La session avec l'id " + requestDto.getSessionId() + " n'existe pas")
//            );
//        }


        TransactionResponseDTO transaction = epargneService.processEpargne(
                requestDto.getMemberId(),
                requestDto.getAmount(),
                requestDto.getTransactionDirection()
        );

        return ResponseEntity.ok(ApiResponseDto.ok(transaction, "Epargne reussie avec succès"));
    }

}
