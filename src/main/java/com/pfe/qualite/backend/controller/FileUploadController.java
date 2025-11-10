package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.Attachment;
import com.pfe.qualite.backend.repository.AttachmentRepository;
import com.pfe.qualite.backend.service.FileStorageService;
import com.pfe.qualite.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des fichiers
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;
    private final JwtUtil jwtUtil;

    // Types de fichiers autorisés
    private static final String[] ALLOWED_TYPES = {
        "image/jpeg", "image/png", "image/gif",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    // Taille maximale: 10 MB
    private static final long MAX_FILE_SIZE_MB = 10;

    /**
     * Upload un fichier
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") String entityId,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        
        try {
            // Validation du fichier
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Le fichier est vide"));
            }

            // Vérifier le type de fichier
            if (!fileStorageService.isValidFileType(file, ALLOWED_TYPES)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Type de fichier non autorisé"));
            }

            // Vérifier la taille du fichier
            if (!fileStorageService.isValidFileSize(file, MAX_FILE_SIZE_MB)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Fichier trop volumineux (max " + MAX_FILE_SIZE_MB + " MB)"));
            }

            // Stocker le fichier
            String storedFileName = fileStorageService.storeFile(file);

            // Créer l'enregistrement dans la base de données
            Attachment attachment = Attachment.builder()
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .entityType(entityType)
                    .entityId(entityId)
                    .uploadedBy(getUserIdFromRequest(request))
                    .uploadedAt(new Date())
                    .description(description)
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);

            log.info("Fichier uploadé avec succès: {} pour {}/{}", 
                     file.getOriginalFilename(), entityType, entityId);

            return ResponseEntity.ok(savedAttachment);

        } catch (Exception e) {
            log.error("Erreur lors de l'upload du fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    /**
     * Télécharge un fichier
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId, HttpServletRequest request) {
        try {
            // Récupérer l'attachment depuis la base de données
            Attachment attachment = attachmentRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            // Charger le fichier
            Resource resource = fileStorageService.loadFileAsResource(attachment.getStoredFileName());

            // Déterminer le type de contenu
            String contentType = attachment.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du fichier: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupère tous les fichiers d'une entité
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<Attachment>> getEntityFiles(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        
        List<Attachment> attachments = attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Récupère les informations d'un fichier
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Attachment> getFileInfo(@PathVariable String fileId) {
        return attachmentRepository.findById(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime un fichier
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, HttpServletRequest request) {
        try {
            Attachment attachment = attachmentRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Fichier non trouvé"));

            // Vérifier les permissions: seul le créateur ou un admin peut supprimer
            String userId = getUserIdFromRequest(request);
            String userRole = jwtUtil.extractRoleFromRequest(request);
            
            // Vérification: Admin peut tout supprimer, sinon vérifier le créateur
            if (!"ADMIN".equals(userRole) && !userId.equals(attachment.getUploadedBy())) {
                log.warn("Tentative de suppression non autorisée par {} pour le fichier {}", userId, fileId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Vous n'avez pas la permission de supprimer ce fichier"));
            }

            // Supprimer le fichier physique
            fileStorageService.deleteFile(attachment.getStoredFileName());

            // Supprimer l'enregistrement de la base de données
            attachmentRepository.deleteById(fileId);

            log.info("Fichier supprimé: {} par {}", attachment.getOriginalFileName(), userId);

            return ResponseEntity.ok(createSuccessResponse("Fichier supprimé avec succès"));

        } catch (Exception e) {
            log.error("Erreur lors de la suppression du fichier: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    /**
     * Supprime tous les fichiers d'une entité
     */
    @DeleteMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> deleteEntityFiles(
            @PathVariable String entityType,
            @PathVariable String entityId,
            HttpServletRequest request) {
        
        try {
            List<Attachment> attachments = attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);

            // Supprimer les fichiers physiques
            for (Attachment attachment : attachments) {
                fileStorageService.deleteFile(attachment.getStoredFileName());
            }

            // Supprimer les enregistrements
            attachmentRepository.deleteByEntityTypeAndEntityId(entityType, entityId);

            log.info("Fichiers supprimés pour {}/{}: {} fichier(s)", 
                     entityType, entityId, attachments.size());

            return ResponseEntity.ok(createSuccessResponse(attachments.size() + " fichier(s) supprimé(s)"));

        } catch (Exception e) {
            log.error("Erreur lors de la suppression des fichiers de l'entité {}/{}", 
                      entityType, entityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    // ==================== Méthodes utilitaires ====================

    /**
     * Extrait l'ID utilisateur de la requête (depuis le token JWT)
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        return jwtUtil.extractUserIdFromRequest(request);
    }

    /**
     * Crée une réponse d'erreur
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    /**
     * Crée une réponse de succès
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
