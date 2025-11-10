package com.pfe.qualite.backend.security.auth;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import com.pfe.qualite.backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtils jwtUtils;

    public String login(LoginRequest request) {
        log.info("Tentative de login pour email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Échec d'authentification pour {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        log.info("Utilisateur trouvé: {} | Rôle: {}", utilisateur.getEmail(), utilisateur.getRole());
        return jwtUtils.generateToken(utilisateur.getId(), utilisateur.getEmail(), utilisateur.getRole());
    }
}
