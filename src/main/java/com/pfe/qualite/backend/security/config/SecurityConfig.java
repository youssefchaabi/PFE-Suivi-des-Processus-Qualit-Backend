package com.pfe.qualite.backend.security.config;

import com.pfe.qualite.backend.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.cors.*;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ Config CORS personnalisée ici
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )
                .authorizeHttpRequests(authz -> authz
                        // Autoriser les préflight CORS sans auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Lecture (GET) autorisée à tous les rôles sur tous les modules
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/fiches", "/api/fiches/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/suivis", "/api/suivis/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                           .requestMatchers("/api/ai-analytics/**").hasRole("PILOTE_QUALITE")
                        .requestMatchers("/api/ai-charts/**").hasRole("PILOTE_QUALITE")
                        .requestMatchers("/api/chat/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        .requestMatchers("/api/historique/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        // Autoriser les opérations POST nécessaires pour Historique (filtres, export)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/historique/**").hasAnyRole("ADMIN", "PILOTE_QUALITE")
                        .requestMatchers("/api/formulaires-obligatoires/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        .requestMatchers("/api/rapports-kpi/**").hasRole("PILOTE_QUALITE")
                        // CRUD Nomenclatures réservé à l'ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/nomenclatures", "/api/nomenclatures/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/nomenclatures/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/nomenclatures/**").hasRole("ADMIN")
                        // CRUD Utilisateurs réservé à l'ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/utilisateurs", "/api/admin/create-user").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/utilisateurs/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/utilisateurs/**").hasRole("ADMIN")
                        // CRUD Fiche Qualité, Fiche Suivi : CHEF_PROJET (sur ses éléments) et ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/fiches", "/api/suivis").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/fiches/**", "/api/suivis/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/fiches/**", "/api/suivis/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        // CRUD Fiche Projet : CHEF_PROJET (sur ses projets) et ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projets").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projets/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projets/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        // CRUD Tâches : CHEF_PROJET et ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/taches").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/taches/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/taches/**").hasAnyRole("ADMIN", "CHEF_PROJET")
                        // Notifications :
                        // - POST /relancer (relance email) : ADMIN, CHEF_PROJET, PILOTE_QUALITE
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/notifications/relancer").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        // - POST (création système) : ADMIN
                        // - PUT (marquer lue) : ADMIN, CHEF_PROJET, PILOTE_QUALITE
                        // - DELETE (suppression) : ADMIN, CHEF_PROJET, PILOTE_QUALITE (vérification propriétaire côté contrôleur)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/notifications", "/api/notifications/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/notifications/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/notifications/**").hasAnyRole("ADMIN", "CHEF_PROJET", "PILOTE_QUALITE")
                        // Catch-all : ADMIN a tous les droits
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Autoriser plusieurs origines de développement (localhost, 127.0.0.1, ports courants, réseau local)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:8081",
                "http://127.0.0.1:8081",
                "http://192.168.*:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Non authentifié", 
                "Token manquant ou invalide");
    }

    @Bean
    public AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) -> writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Accès refusé",
                "Rôle insuffisant pour cette ressource");
    }

    private void writeJsonError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = String.format("{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}", status, escapeJson(error), escapeJson(message));
        response.getWriter().write(body);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utiliser 4 rounds au lieu de 10 par défaut pour être plus rapide
        // 4 rounds = ~50ms au lieu de ~500ms pour 10 rounds
        // Toujours sécurisé pour un environnement de développement/test
        return new BCryptPasswordEncoder(4);
    }
}