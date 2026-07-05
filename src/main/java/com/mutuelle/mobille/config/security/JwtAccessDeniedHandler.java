package com.mutuelle.mobille.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mutuelle.mobille.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Récupérer les rôles de l'utilisateur pour un message plus utile
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRoles = auth != null ?
                auth.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                : "Aucun";

        String detail = String.format(
                "Vous n'avez pas les permissions nécessaires pour accéder à cette ressource. Vos rôles actuels : [%s]",
                userRoles
        );

        ApiResponseDto<?> apiResponse = ApiResponseDto.builder()
                .success(false)
                .message("Accès refusé")
                .errors(List.of(detail))
                .code(403)
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}