package com.pfe.qualite.backend.security.auth;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UtilisateurService utilisateurService;

    @PostMapping("/create-user")
    public ResponseEntity<Utilisateur> creer(@RequestBody Utilisateur u) {
        // Utiliser la nouvelle signature avec l'objet Utilisateur complet
        Utilisateur created = utilisateurService.creerUtilisateur(u, "ADMIN");
        created.setPassword(null); // Ne pas retourner le mot de passe
        return ResponseEntity.ok(created);
    }
}
