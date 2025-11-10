package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FicheProjet;
import com.pfe.qualite.backend.repository.FicheProjetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FicheProjetService {
    @Autowired
    private FicheProjetRepository ficheProjetRepository;

    public List<FicheProjet> getAll() {
        return ficheProjetRepository.findAll();
    }

    public Optional<FicheProjet> getById(String id) {
        return ficheProjetRepository.findById(id);
    }

    public FicheProjet create(FicheProjet ficheProjet) {
        return ficheProjetRepository.save(ficheProjet);
    }

    public FicheProjet update(String id, FicheProjet updated) {
        return ficheProjetRepository.findById(id).map(fp -> {
            fp.setNom(updated.getNom());
            fp.setDescription(updated.getDescription());
            fp.setObjectifs(updated.getObjectifs());
            fp.setResponsable(updated.getResponsable());
            fp.setEcheance(updated.getEcheance());
            fp.setStatut(updated.getStatut());
            return ficheProjetRepository.save(fp);
        }).orElseThrow(() -> new RuntimeException("Fiche projet non trouvée"));
    }

    public void delete(String id) {
        ficheProjetRepository.deleteById(id);
    }
} 