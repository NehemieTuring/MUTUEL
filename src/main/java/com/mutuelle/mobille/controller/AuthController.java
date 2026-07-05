package com.mutuelle.mobille.controller;

import com.mutuelle.mobille.dto.*;
import com.mutuelle.mobille.dto.auth.*;
import com.mutuelle.mobille.dto.member.MemberRegisterDTO;
import com.mutuelle.mobille.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.mutuelle.mobille.exception.AccountDisabledException;
import com.mutuelle.mobille.exception.AccountInactiveException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion de l'utilisateur")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {

        try {
            LoginResponseDto loginData = authService.login(
                    request.getEmail(),
                    request.getPassword()
            );
            return ResponseEntity.ok(ApiResponseDto.ok(loginData, "Connexion réussie"));
        } catch (AccountDisabledException | AccountInactiveException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseDto.errorCustom(e.getMessage(), 403));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.ok(ApiResponseDto.errorCustom("Email ou mot de passe incorrect", 400));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.errorCustom("Email ou mot de passe incorrect", 400));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> refresh(@Valid @RequestBody RefreshRequestDto request) {
        String newAccessToken = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponseDto.ok(new TokenResponseDto(newAccessToken), "Token rafraîchi avec succès"));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Déconnexion de l'utilisateur (invalide tous ses refresh tokens)")
    public ResponseEntity<ApiResponseDto<Void>> logout() {

        authService.logoutCurrentUser();

        return ResponseEntity.ok(
                ApiResponseDto.ok(null, "Déconnexion réussie")
        );
    }
}