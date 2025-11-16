package com.pfe.qualite.backend.security.auth;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import com.pfe.qualite.backend.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 * Utilise Mockito pour simuler les dépendances
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Utilisateur utilisateurTest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Configuration des propriétés
        ReflectionTestUtils.setField(authService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200");

        // Création d'un utilisateur de test
        utilisateurTest = new Utilisateur();
        utilisateurTest.setId("user123");
        utilisateurTest.setEmail("test@example.com");
        utilisateurTest.setNom("Test User");
        utilisateurTest.setPassword("encodedPassword");
        utilisateurTest.setRole("ADMIN");

        // Création d'une requête de login
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLogin_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(utilisateurRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(utilisateurTest));
        when(jwtUtils.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("jwt-token-123");

        // When
        String token = authService.login(loginRequest);

        // Then
        assertNotNull(token);
        assertEquals("jwt-token-123", token);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(utilisateurRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtUtils, times(1)).generateToken(
                utilisateurTest.getId(),
                utilisateurTest.getEmail(),
                utilisateurTest.getRole()
        );
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(authenticationManager, times(1)).authenticate(any());
        verify(utilisateurRepository, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(utilisateurRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.login(loginRequest));
        assertEquals("Utilisateur introuvable", exception.getMessage());
        verify(utilisateurRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    void testForgotPassword_Success() {
        // Given
        when(utilisateurRepository.findByEmail(utilisateurTest.getEmail()))
                .thenReturn(Optional.of(utilisateurTest));
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenReturn(utilisateurTest);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        authService.forgotPassword(utilisateurTest.getEmail());

        // Then
        assertNotNull(utilisateurTest.getResetPasswordToken());
        assertNotNull(utilisateurTest.getResetPasswordTokenExpiry());
        verify(utilisateurRepository, times(1)).findByEmail(utilisateurTest.getEmail());
        verify(utilisateurRepository, times(1)).save(utilisateurTest);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testForgotPassword_UserNotFound() {
        // Given
        when(utilisateurRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.forgotPassword("nonexistent@example.com"));
        assertEquals("Aucun compte associé à cet e-mail", exception.getMessage());
        verify(utilisateurRepository, times(1)).findByEmail(anyString());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testResetPassword_Success() {
        // Given
        String token = "valid-token-123";
        String newPassword = "newPassword123";
        utilisateurTest.setResetPasswordToken(token);
        utilisateurTest.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));

        when(utilisateurRepository.findByResetPasswordToken(token))
                .thenReturn(Optional.of(utilisateurTest));
        when(passwordEncoder.encode(newPassword))
                .thenReturn("encodedNewPassword");
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenReturn(utilisateurTest);

        // When
        authService.resetPassword(token, newPassword);

        // Then
        assertNull(utilisateurTest.getResetPasswordToken());
        assertNull(utilisateurTest.getResetPasswordTokenExpiry());
        verify(utilisateurRepository, times(1)).findByResetPasswordToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(utilisateurRepository, times(1)).save(utilisateurTest);
    }

    @Test
    void testResetPassword_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(utilisateurRepository.findByResetPasswordToken(invalidToken))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.resetPassword(invalidToken, "newPassword"));
        assertEquals("Token invalide ou expiré", exception.getMessage());
        verify(utilisateurRepository, times(1)).findByResetPasswordToken(invalidToken);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testResetPassword_ExpiredToken() {
        // Given
        String expiredToken = "expired-token";
        utilisateurTest.setResetPasswordToken(expiredToken);
        utilisateurTest.setResetPasswordTokenExpiry(LocalDateTime.now().minusHours(2));

        when(utilisateurRepository.findByResetPasswordToken(expiredToken))
                .thenReturn(Optional.of(utilisateurTest));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.resetPassword(expiredToken, "newPassword"));
        assertTrue(exception.getMessage().contains("expiré"));
        verify(utilisateurRepository, times(1)).findByResetPasswordToken(expiredToken);
        verify(passwordEncoder, never()).encode(anyString());
    }
}
