package com.mutuelle.mobille.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mutuelle.mobille.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Support pour LocalDateTime

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Détecter le type d'erreur pour un message plus précis
        String message = "Authentification requise";
        String detail = null;

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            detail = "Aucun token fourni. Veuillez vous connecter.";
        } else if (!authHeader.startsWith("Bearer ")) {
            detail = "Format du token invalide. Utilisez 'Bearer <token>'.";
        } else {
            detail = "Token invalide ou expiré. Veuillez vous reconnecter.";
        }

        ApiResponseDto<?> apiResponse = ApiResponseDto.builder()
                .success(false)
                .message(message)
                .errors(detail != null ? List.of(detail) : null)
                .code(401)
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}