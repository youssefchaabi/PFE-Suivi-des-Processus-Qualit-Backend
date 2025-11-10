package com.pfe.qualite.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service d'export de données en Excel et PDF
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    /**
     * Exporte des données en format Excel
     */
    public byte[] exportToExcel(String sheetName, List<String> headers, List<List<Object>> data) throws IOException {
        log.info("Export Excel - Sheet: {}, Lignes: {}", sheetName, data.size());
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Style pour les données
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Créer la ligne d'en-tête
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Créer les lignes de données
            int rowNum = 1;
            for (List<Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                    
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // Auto-dimensionner les colonnes
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            log.info("Export Excel réussi - Taille: {} bytes", out.size());
            return out.toByteArray();
        }
    }

    /**
     * Exporte un rapport KPI en Excel
     */
    public byte[] exportRapportKpiToExcel(Map<String, Object> rapportData) throws IOException {
        log.info("Export Rapport KPI vers Excel");
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Feuille 1: Statistiques Générales
            createStatistiquesGeneralesSheet(workbook, rapportData);
            
            // Feuille 2: Statistiques par Statut
            createStatistiquesParStatutSheet(workbook, rapportData);
            
            // Feuille 3: Évolution Temporelle
            createEvolutionTemporelleSheet(workbook, rapportData);
            
            // Feuille 4: Métriques de Performance
            createMetriquesPerformanceSheet(workbook, rapportData);
            
            workbook.write(out);
            log.info("Export Rapport KPI Excel réussi - Taille: {} bytes", out.size());
            return out.toByteArray();
        }
    }

    /**
     * Crée la feuille des statistiques générales
     */
    private void createStatistiquesGeneralesSheet(Workbook workbook, Map<String, Object> rapportData) {
        Sheet sheet = workbook.createSheet("Statistiques Générales");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) rapportData.get("statistiquesGenerales");
        
        if (stats != null) {
            int rowNum = 0;
            
            // En-tête
            Row headerRow = sheet.createRow(rowNum++);
            createCell(headerRow, 0, "Métrique", headerStyle);
            createCell(headerRow, 1, "Valeur", headerStyle);
            
            // Données
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, formatKey(entry.getKey()), dataStyle);
                createCell(row, 1, entry.getValue().toString(), dataStyle);
            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }
    }

    /**
     * Crée la feuille des statistiques par statut
     */
    private void createStatistiquesParStatutSheet(Workbook workbook, Map<String, Object> rapportData) {
        Sheet sheet = workbook.createSheet("Statistiques par Statut");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) rapportData.get("statistiquesParStatut");
        
        if (stats != null) {
            int rowNum = 0;
            
            // En-tête
            Row headerRow = sheet.createRow(rowNum++);
            createCell(headerRow, 0, "Statut", headerStyle);
            createCell(headerRow, 1, "Nombre", headerStyle);
            
            // Statuts Qualité
            @SuppressWarnings("unchecked")
            Map<String, Long> statutsQualite = (Map<String, Long>) stats.get("statutsQualite");
            if (statutsQualite != null) {
                for (Map.Entry<String, Long> entry : statutsQualite.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, entry.getKey(), dataStyle);
                    createCell(row, 1, entry.getValue().toString(), dataStyle);
                }
            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }
    }

    /**
     * Crée la feuille de l'évolution temporelle
     */
    private void createEvolutionTemporelleSheet(Workbook workbook, Map<String, Object> rapportData) {
        Sheet sheet = workbook.createSheet("Évolution Temporelle");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> evolution = (Map<String, Object>) rapportData.get("evolutionTemporelle");
        
        if (evolution != null) {
            int rowNum = 0;
            
            // En-tête
            Row headerRow = sheet.createRow(rowNum++);
            createCell(headerRow, 0, "Période", headerStyle);
            createCell(headerRow, 1, "Nombre de Fiches", headerStyle);
            
            @SuppressWarnings("unchecked")
            Map<String, Long> fichesParMois = (Map<String, Long>) evolution.get("fichesParMois");
            if (fichesParMois != null) {
                for (Map.Entry<String, Long> entry : fichesParMois.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, entry.getKey(), dataStyle);
                    createCell(row, 1, entry.getValue().toString(), dataStyle);
                }
            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }
    }

    /**
     * Crée la feuille des métriques de performance
     */
    private void createMetriquesPerformanceSheet(Workbook workbook, Map<String, Object> rapportData) {
        Sheet sheet = workbook.createSheet("Métriques Performance");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metriques = (Map<String, Object>) rapportData.get("metriquesPerformance");
        
        if (metriques != null) {
            int rowNum = 0;
            
            // En-tête
            Row headerRow = sheet.createRow(rowNum++);
            createCell(headerRow, 0, "Métrique", headerStyle);
            createCell(headerRow, 1, "Valeur", headerStyle);
            
            // Données
            for (Map.Entry<String, Object> entry : metriques.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, formatKey(entry.getKey()), dataStyle);
                
                Object value = entry.getValue();
                String valueStr = value instanceof Number 
                    ? String.format("%.2f", ((Number) value).doubleValue()) 
                    : value.toString();
                createCell(row, 1, valueStr, dataStyle);
            }
            
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        }
    }

    /**
     * Crée un style pour les en-têtes
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Crée un style pour les données
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crée une cellule avec valeur et style
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Formate une clé en texte lisible
     */
    private String formatKey(String key) {
        return key.replaceAll("([A-Z])", " $1")
                  .replaceAll("^.", String.valueOf(Character.toUpperCase(key.charAt(0))))
                  .trim();
    }
}
