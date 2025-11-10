package com.pfe.qualite.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service de gestion du stockage des fichiers
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Répertoire de stockage créé: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Impossible de créer le répertoire de stockage", ex);
            throw new RuntimeException("Impossible de créer le répertoire de stockage", ex);
        }
    }

    /**
     * Stocke un fichier et retourne le nom généré
     */
    public String storeFile(MultipartFile file) {
        // Nettoyer le nom du fichier
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Vérifier si le fichier contient des caractères invalides
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Le nom du fichier contient une séquence de chemin invalide: " + originalFileName);
            }

            // Générer un nom unique pour éviter les conflits
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Copier le fichier vers le répertoire de destination
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Fichier stocké: {} (original: {})", uniqueFileName, originalFileName);
            return uniqueFileName;
            
        } catch (IOException ex) {
            log.error("Erreur lors du stockage du fichier: {}", originalFileName, ex);
            throw new RuntimeException("Erreur lors du stockage du fichier: " + originalFileName, ex);
        }
    }

    /**
     * Charge un fichier en tant que Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé: " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("Fichier non trouvé: {}", fileName, ex);
            throw new RuntimeException("Fichier non trouvé: " + fileName, ex);
        }
    }

    /**
     * Supprime un fichier
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Fichier supprimé: {}", fileName);
        } catch (IOException ex) {
            log.error("Erreur lors de la suppression du fichier: {}", fileName, ex);
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + fileName, ex);
        }
    }

    /**
     * Vérifie si un fichier existe
     */
    public boolean fileExists(String fileName) {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    /**
     * Obtient la taille d'un fichier en bytes
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            log.error("Erreur lors de la récupération de la taille du fichier: {}", fileName, ex);
            return 0;
        }
    }

    /**
     * Valide le type de fichier
     */
    public boolean isValidFileType(MultipartFile file, String[] allowedTypes) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : allowedTypes) {
            if (contentType.toLowerCase().contains(allowedType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Valide la taille du fichier (en MB)
     */
    public boolean isValidFileSize(MultipartFile file, long maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        return file.getSize() <= maxSizeBytes;
    }
}
