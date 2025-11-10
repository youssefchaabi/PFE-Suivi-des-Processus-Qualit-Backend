package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.service.ExportService;
import com.pfe.qualite.backend.service.RapportKpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Contrôleur pour l'export de rapports
 * Désactivé temporairement - Sprint 2 (dépend de RapportKpiService)
 */
// @RestController  // Désactivé temporairement
@RequestMapping("/api/export")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final ExportService exportService;
    private final RapportKpiService rapportKpiService;

    /**
     * Exporte le rapport KPI complet en Excel
     */
    @GetMapping("/rapport-kpi/excel")
    public ResponseEntity<byte[]> exportRapportKpiExcel() {
        try {
            log.info("Demande d'export du rapport KPI en Excel");
            
            // Générer le rapport KPI
            Map<String, Object> rapportData = rapportKpiService.genererRapportKpiComplet();
            
            // Exporter en Excel
            byte[] excelBytes = exportService.exportRapportKpiToExcel(rapportData);
            
            // Générer le nom du fichier avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "rapport_kpi_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);
            
            log.info("Export Excel réussi: {} ({} bytes)", filename, excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel du rapport KPI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporte les statistiques générales en Excel
     */
    @GetMapping("/statistiques/excel")
    public ResponseEntity<byte[]> exportStatistiquesExcel() {
        try {
            log.info("Demande d'export des statistiques en Excel");
            
            Map<String, Object> rapportData = rapportKpiService.genererRapportKpiComplet();
            byte[] excelBytes = exportService.exportRapportKpiToExcel(rapportData);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "statistiques_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel des statistiques", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
