package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.HistoriqueAction;
import com.pfe.qualite.backend.service.HistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/historique")
@CrossOrigin(origins = "*")
public class HistoriqueController {

    @Autowired
    private HistoriqueService historiqueService;

    /**
     * Récupère l'ensemble de l'historique
     */
    @GetMapping("")
    public ResponseEntity<List<HistoriqueAction>> getAll() {
        return ResponseEntity.ok(historiqueService.getAll());
    }

    /**
     * Récupère l'historique d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueUtilisateur(@PathVariable String utilisateurId) {
        List<HistoriqueAction> historique = historiqueService.getHistoriqueUtilisateur(utilisateurId);
        return ResponseEntity.ok(historique);
    }

    /**
     * Récupère l'historique d'une entité
     */
    @GetMapping("/entite/{entite}/{entiteId}")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueEntite(
            @PathVariable String entite, 
            @PathVariable String entiteId) {
        List<HistoriqueAction> historique = historiqueService.getHistoriqueEntite(entite, entiteId);
        return ResponseEntity.ok(historique);
    }

    /**
     * Récupère l'historique par période
     */
    @GetMapping("/periode")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueParPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        List<HistoriqueAction> historique = historiqueService.getHistoriqueParPeriode(dateDebut, dateFin);
        return ResponseEntity.ok(historique);
    }

    /**
     * Récupère l'historique d'un utilisateur par période
     */
    @GetMapping("/utilisateur/{utilisateurId}/periode")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueUtilisateurParPeriode(
            @PathVariable String utilisateurId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        List<HistoriqueAction> historique = historiqueService.getHistoriqueUtilisateurParPeriode(utilisateurId, dateDebut, dateFin);
        return ResponseEntity.ok(historique);
    }

    /**
     * Récupère l'historique d'une entité par période
     */
    @GetMapping("/entite/{entite}/periode")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueEntiteParPeriode(
            @PathVariable String entite,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        List<HistoriqueAction> historique = historiqueService.getHistoriqueEntiteParPeriode(entite, dateDebut, dateFin);
        return ResponseEntity.ok(historique);
    }

    /**
     * Récupère les statistiques d'actions d'un utilisateur
     */
    @GetMapping("/stats/utilisateur/{utilisateurId}")
    public ResponseEntity<Long> getNombreActionsUtilisateur(@PathVariable String utilisateurId) {
        long nombre = historiqueService.getNombreActionsUtilisateur(utilisateurId);
        return ResponseEntity.ok(nombre);
    }

    /**
     * Récupère les statistiques d'actions d'une entité
     */
    @GetMapping("/stats/entite/{entite}")
    public ResponseEntity<Long> getNombreActionsEntite(@PathVariable String entite) {
        long nombre = historiqueService.getNombreActionsEntite(entite);
        return ResponseEntity.ok(nombre);
    }

    /**
     * Stats globales: total, aujourd'hui, semaine, mois
     */
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotal() {
        return ResponseEntity.ok(historiqueService.countAll());
    }

    @GetMapping("/stats/aujourd-hui")
    public ResponseEntity<Long> getToday() {
        LocalDate today = LocalDate.now();
        Date start = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(historiqueService.countBetween(start, end));
    }

    @GetMapping("/stats/semaine")
    public ResponseEntity<Long> getThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        Date start = Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(startOfWeek.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(historiqueService.countBetween(start, end));
    }

    @GetMapping("/stats/mois")
    public ResponseEntity<Long> getThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        Date start = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(startOfMonth.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(historiqueService.countBetween(start, end));
    }

    /**
     * Filtres avancés
     */
    @PostMapping("/filtres")
    public ResponseEntity<List<HistoriqueAction>> getHistoriqueFiltres(@RequestBody FiltresHistoriqueRequest filtres) {
        return ResponseEntity.ok(historiqueService.getHistoriqueFiltres(filtres));
    }

    /**
     * Export CSV simple (servi en text/csv)
     */
    @PostMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody(required = false) FiltresHistoriqueRequest filtres) {
        List<HistoriqueAction> data = (filtres != null) ? historiqueService.getHistoriqueFiltres(filtres) : historiqueService.getAll();
        StringBuilder sb = new StringBuilder();
        sb.append("date;utilisateur;action;module;entiteId;details\n");
        for (HistoriqueAction a : data) {
            sb.append(escape(a.getDateAction()))
              .append(';').append(escape(a.getUtilisateurNom()))
              .append(';').append(escape(a.getAction()))
              .append(';').append(escape(a.getEntite()))
              .append(';').append(escape(a.getEntiteId()))
              .append(';').append(escape(a.getDetails()))
              .append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historique.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private String escape(Object value) {
        if (value == null) return "";
        String s = value.toString();
        return s.replace(";", ",").replace("\n", " ");
    }

    public static class FiltresHistoriqueRequest {
        public String typeAction;
        public String module;
        public String utilisateurId;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        public Date dateDebut;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        public Date dateFin;
        public String periode;
    }
} 