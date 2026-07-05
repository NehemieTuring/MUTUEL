package com.mutuelle.mobille.config.security;

import com.mutuelle.mobille.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si pas de token → on ne fait RIEN → SecurityContext reste vide → Spring déclenchera 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Il y a un header Bearer → on essaie de valider le token
        String jwt = authHeader.substring(7);

        try {
            String email = jwtUtils.getEmailFromToken(jwt);

            // Important : on vérifie que l'utilisateur n'est pas déjà authentifié
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.isTokenValid(jwt)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // On authentifie l'utilisateur
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                // Si token invalide/expiré → on ne fait rien → SecurityContext reste vide → 401
            }
        } catch (Exception e) {
            // Token invalide ou corrompu → on ne fait rien → SecurityContext vide → 401
            // Tu peux logger ici si tu veux
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Chemins publics qui ne nécessitent pas de token JWT
        return path.startsWith("/preview/") ||
                path.startsWith("/preview/email/welcome") ||
                path.startsWith("/auth/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/actuator/health") ||
                path.equals("/api/config/current") ;
    }
}