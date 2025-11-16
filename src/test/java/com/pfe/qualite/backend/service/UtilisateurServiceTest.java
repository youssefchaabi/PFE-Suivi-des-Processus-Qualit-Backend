package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UtilisateurService
 * Couvre les opérations CRUD et la gestion des utilisateurs
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateurTest;

    @BeforeEach
    void setUp() {
        utilisateurTest = new Utilisateur();
        utilisateurTest.setId("user123");
        utilisateurTest.setEmail("test@example.com");
        utilisateurTest.setNom("Test");
        utilisateurTest.setPrenom("User");
        utilisateurTest.setPassword("encodedPassword");
        utilisateurTest.setRole("ADMIN");
        utilisateurTest.setActif(true);
        utilisateurTest.setDateCreation(LocalDateTime.now());
    }

    @Test
    void testGetAllUtilisateurs() {
        // Given
        List<Utilisateur> utilisateurs = Arrays.asList(utilisateurTest, new Utilisateur());
        when(utilisateurRepository.findAll()).thenReturn(utilisateurs);

        // When
        List<Utilisateur> result = utilisateurService.getAllUtilisateurs();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(utilisateurRepository, times(1)).findAll();
    }

    @Test
    void testGetUtilisateurById_Found() {
        // Given
        when(utilisateurRepository.findById("user123")).thenReturn(Optional.of(utilisateurTest));

        // When
        Optional<Utilisateur> result = utilisateurService.getUtilisateurById("user123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(utilisateurRepository, times(1)).findById("user123");
    }

    @Test
    void testGetUtilisateurById_NotFound() {
        // Given
        when(utilisateurRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Utilisateur> result = utilisateurService.getUtilisateurById("nonexistent");

        // Then
        assertFalse(result.isPresent());
        verify(utilisateurRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testGetUtilisateurByEmail() {
        // Given
        when(utilisateurRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(utilisateurTest));

        // When
        Optional<Utilisateur> result = utilisateurService.getUtilisateurByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("user123", result.get().getId());
        verify(utilisateurRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testCreerUtilisateur_Success() throws InterruptedException {
        // Given
        Utilisateur newUser = new Utilisateur();
        newUser.setEmail("new@example.com");
        newUser.setNom("New");
        newUser.setPrenom("User");
        newUser.setRole("CHEF_PROJET");
        newUser.setPassword("password123");

        when(utilisateurRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(newUser);
        doNothing().when(notificationService).envoyerEmailBienvenue(anyString(), anyString(), anyString());

        // When
        Utilisateur result = utilisateurService.creerUtilisateur(newUser, "admin123");

        // Then
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertTrue(result.getActif());
        assertNotNull(result.getDateCreation());
        assertEquals("admin123", result.getCreePar());
        verify(utilisateurRepository, times(1)).findByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        
        // Attendre l'exécution du thread asynchrone
        Thread.sleep(100);
    }

    @Test
    void testCreerUtilisateur_EmailAlreadyExists() {
        // Given
        Utilisateur newUser = new Utilisateur();
        newUser.setEmail("test@example.com");

        when(utilisateurRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(utilisateurTest));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.creerUtilisateur(newUser, "admin123"));
        assertEquals("Un utilisateur avec cet email existe déjà", exception.getMessage());
        verify(utilisateurRepository, times(1)).findByEmail("test@example.com");
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void testUpdateUtilisateur_Success() {
        // Given
        Utilisateur updateData = new Utilisateur();
        updateData.setNom("Updated");
        updateData.setEmail("updated@example.com");
        updateData.setRole("PILOTE_QUALITE");
        updateData.setTelephone("0123456789");
        updateData.setActif(false);

        when(utilisateurRepository.findById("user123")).thenReturn(Optional.of(utilisateurTest));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurTest);

        // When
        Utilisateur result = utilisateurService.updateUtilisateur("user123", updateData);

        // Then
        assertNotNull(result);
        verify(utilisateurRepository, times(1)).findById("user123");
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testUpdateUtilisateur_NotFound() {
        // Given
        when(utilisateurRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.updateUtilisateur("nonexistent", new Utilisateur()));
        assertTrue(exception.getMessage().contains("non trouvé"));
        verify(utilisateurRepository, times(1)).findById("nonexistent");
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void testToggleActif_Success() {
        // Given
        when(utilisateurRepository.findById("user123")).thenReturn(Optional.of(utilisateurTest));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurTest);
        doNothing().when(notificationService).envoyerNotification(anyString(), anyString(), anyString(), anyString());

        // When
        Utilisateur result = utilisateurService.toggleActif("user123");

        // Then
        assertNotNull(result);
        verify(utilisateurRepository, times(1)).findById("user123");
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testResetMotDePasse_Success() {
        // Given
        when(utilisateurRepository.findById("user123")).thenReturn(Optional.of(utilisateurTest));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurTest);
        doNothing().when(notificationService).envoyerEmailResetPassword(anyString(), anyString(), anyString());

        // When
        String newPassword = utilisateurService.resetMotDePasse("user123");

        // Then
        assertNotNull(newPassword);
        assertTrue(newPassword.startsWith("Temp"));
        verify(utilisateurRepository, times(1)).findById("user123");
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testDeleteUtilisateur_Success() {
        // Given
        when(utilisateurRepository.existsById("user123")).thenReturn(true);
        doNothing().when(utilisateurRepository).deleteById("user123");

        // When
        utilisateurService.deleteUtilisateur("user123");

        // Then
        verify(utilisateurRepository, times(1)).existsById("user123");
        verify(utilisateurRepository, times(1)).deleteById("user123");
    }

    @Test
    void testDeleteUtilisateur_NotFound() {
        // Given
        when(utilisateurRepository.existsById("nonexistent")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilisateurService.deleteUtilisateur("nonexistent"));
        assertTrue(exception.getMessage().contains("non trouvé"));
        verify(utilisateurRepository, times(1)).existsById("nonexistent");
        verify(utilisateurRepository, never()).deleteById(anyString());
    }

    @Test
    void testCountByRole() {
        // Given
        Utilisateur user1 = new Utilisateur();
        user1.setRole("ADMIN");
        Utilisateur user2 = new Utilisateur();
        user2.setRole("ADMIN");
        Utilisateur user3 = new Utilisateur();
        user3.setRole("CHEF_PROJET");

        when(utilisateurRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // When
        long count = utilisateurService.countByRole("ADMIN");

        // Then
        assertEquals(2, count);
        verify(utilisateurRepository, times(1)).findAll();
    }

    @Test
    void testCountActifs() {
        // Given
        Utilisateur user1 = new Utilisateur();
        user1.setActif(true);
        Utilisateur user2 = new Utilisateur();
        user2.setActif(true);
        Utilisateur user3 = new Utilisateur();
        user3.setActif(false);

        when(utilisateurRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));

        // When
        long count = utilisateurService.countActifs();

        // Then
        assertEquals(2, count);
        verify(utilisateurRepository, times(1)).findAll();
    }
}
