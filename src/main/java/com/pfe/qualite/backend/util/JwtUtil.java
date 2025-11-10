package com.pfe.qualite.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Utilitaire pour extraire les informations du JWT
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:VotreCleSecreteTresLongueEtComplexePourJWT2024!}")
    private String jwtSecret;

    /**
     * Obtenir la clé de signature
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extraire le token JWT depuis le header Authorization
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Extraire l'ID utilisateur depuis le token JWT
     */
    public String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Le userId peut être dans "sub" ou "userId" selon votre implémentation
            String userId = claims.get("userId", String.class);
            if (userId == null) {
                userId = claims.getSubject(); // Fallback sur "sub"
            }
            
            return userId;
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction de l'userId depuis le token", e);
            return "SYSTEM"; // Valeur par défaut si extraction échoue
        }
    }

    /**
     * Extraire l'ID utilisateur depuis la requête HTTP
     */
    public String extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return extractUserIdFromToken(token);
        }
        return "SYSTEM"; // Valeur par défaut
    }

    /**
     * Extraire le rôle depuis le token JWT
     */
    public String extractRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du rôle depuis le token", e);
            return null;
        }
    }

    /**
     * Extraire le rôle depuis la requête HTTP
     */
    public String extractRoleFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return extractRoleFromToken(token);
        }
        return null;
    }

    /**
     * Extraire l'email depuis le token JWT
     */
    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String email = claims.get("email", String.class);
            if (email == null) {
                email = claims.getSubject(); // Fallback sur "sub"
            }
            
            return email;
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction de l'email depuis le token", e);
            return null;
        }
    }
}
