package com.mutuelle.mobille.utils;

import com.mutuelle.mobille.models.auth.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(AuthUser user) {
        return Jwts.builder()
                .subject(user.getEmail())                    // ← email dans le subject (standard)
                .claim("userId", user.getId())
                .claim("type", user.getRole().name())
                .claim("refId", user.getUserRefId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(AuthUser user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // --- Méthodes utilitaires génériques ---
    private Claims extractAllClaims(String token) {
        return parseToken(token).getPayload();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return getClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String getUserTypeFromToken(String token) {
        return getClaim(token, claims -> claims.get("type", String.class));
    }

    public Long getRefIdFromToken(String token) {
        return getClaim(token, claims -> claims.get("refId", Long.class));
    }

    // --- Parsing et validation ---
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}