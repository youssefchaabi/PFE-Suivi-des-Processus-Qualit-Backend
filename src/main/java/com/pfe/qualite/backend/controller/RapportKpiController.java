package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.service.RapportKpiService;
import com.pfe.qualite.backend.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Contrôleur REST pour la génération et l'export de rapports KPI
 * Désactivé temporairement - Sprint 2 (dépend de RapportKpiService)
 */
// @RestController  // Désactivé temporairement
@RequestMapping("/api/rapports-kpi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RapportKpiController {

    private final RapportKpiService rapportKpiService;
    private final ExportService exportService;

    /**
     * Générer un rapport KPI complet
     */
    @GetMapping("/complet")
    public ResponseEntity<Map<String, Object>> genererRapportKpiComplet() {
        Map<String, Object> rapport = rapportKpiService.genererRapportKpiComplet();
        return ResponseEntity.ok(rapport);
    }

    /**
     * Générer un rapport KPI par période
     */
    @GetMapping("/periode")
    public ResponseEntity<Map<String, Object>> genererRapportParPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        Map<String, Object> rapport = rapportKpiService.genererRapportParPeriode(dateDebut, dateFin);
        return ResponseEntity.ok(rapport);
    }

    /**
     * Télécharger le rapport KPI en JSON
     */
    @GetMapping("/telecharger")
    public ResponseEntity<Map<String, Object>> telechargerRapportKpi() {
        Map<String, Object> rapport = rapportKpiService.genererRapportKpiComplet();
        return ResponseEntity.ok(rapport);
    }

    /**
     * Télécharger le rapport KPI par période en JSON
     */
    @GetMapping("/telecharger/periode")
    public ResponseEntity<Map<String, Object>> telechargerRapportParPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        Map<String, Object> rapport = rapportKpiService.genererRapportParPeriode(dateDebut, dateFin);
        return ResponseEntity.ok(rapport);
    }

    /**
     * Exporter le rapport KPI en Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exporterRapportExcel() {
        try {
            Map<String, Object> rapport = rapportKpiService.genererRapportKpiComplet();
            byte[] excelData = exportService.exportRapportKpiToExcel(rapport);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "rapport-kpi.xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporter le rapport KPI par période en Excel
     */
    @GetMapping("/export/excel/periode")
    public ResponseEntity<byte[]> exporterRapportExcelParPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        try {
            Map<String, Object> rapport = rapportKpiService.genererRapportParPeriode(dateDebut, dateFin);
            byte[] excelData = exportService.exportRapportKpiToExcel(rapport);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "rapport-kpi-periode.xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 