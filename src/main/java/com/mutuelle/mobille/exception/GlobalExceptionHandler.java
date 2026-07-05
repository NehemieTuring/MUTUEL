package com.mutuelle.mobille.exception;

import com.mutuelle.mobille.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. ERREURS DE VALIDATION (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ApiResponseDto<Object> response = ApiResponseDto.validationError(errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        // ou .badRequest() si tu préfères 400 au lieu de 422
    }

    // 2. Mauvais login / mot de passe + token invalide/expiré
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponseDto<?>> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.unauthorized("Email ou mot de passe incorrect"));
    }

    // 3. Accès refusé (connecté mais pas les droits)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.builder()
                        .success(false)
                        .message("Accès refusé")
                        .code(403)
                        .errors(List.of("Vous n'avez pas les permissions nécessaires"))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // 4. Compte bloqué / désactivé / inexistant
    @ExceptionHandler({UsernameNotFoundException.class, DisabledException.class, LockedException.class})
    public ResponseEntity<ApiResponseDto<?>> handleAccountIssues(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.unauthorized("Compte désactivé, bloqué ou introuvable"));
    }

    // 5. Tes RuntimeException personnalisées (ex: refresh token expiré)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<?>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Erreur inconnue";

        if (msg.toLowerCase().contains("refresh token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.unauthorized(msg));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.badRequest("Opération échouée : " + msg));
    }

    // 6. Vraie erreur serveur (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleGenericException(Exception ex) {
        // Affiche l'erreur dans les logs de Render pour vous
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("DEBUG: " + ex.getMessage())); // Affiche l'erreur sur Swagger
    }

    //  @ExceptionHandler(Exception.class)
    // public ResponseEntity<ApiResponseDto<?>> handleGenericException(Exception ex) {
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(ApiResponseDto.error("Une erreur interne est survenue. Veuillez réessayer plus tard."));
    // }



    // Compte désactivé administrativement
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAccountDisabled(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .code(403)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // Compte inactif pour dette
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAccountInactive(AccountInactiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .code(403)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
