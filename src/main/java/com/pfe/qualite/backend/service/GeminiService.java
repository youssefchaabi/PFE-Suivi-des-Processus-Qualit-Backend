package com.pfe.qualite.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.model.Notification;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import com.pfe.qualite.backend.repository.FicheSuiviRepository;
import com.pfe.qualite.backend.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent}")
    private String geminiApiUrl;
    
    @Value("${gemini.max.retries:2}")
    private int maxRetries;
    
    @Value("${gemini.retry.delay:2000}")
    private long retryDelay;

    @Autowired
    private FicheQualiteRepository ficheQualiteRepository;

    @Autowired
    private FicheSuiviRepository ficheSuiviRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Log l'initialisation au premier appel
    private boolean initialized = false;
    
    private void logInitialization() {
        if (!initialized) {
            logger.info("=== GEMINI SERVICE INITIALIZATION ===");
            logger.info("API URL: {}", geminiApiUrl);
            if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
                logger.info("✓ API Key: Configured (length: {} chars)", geminiApiKey.length());
                logger.info("✓ Gemini API is ENABLED - Will use real AI responses");
            } else {
                logger.warn("⚠️ API Key: NOT configured");
                logger.warn("⚠️ Gemini API is DISABLED - Will use intelligent mock responses based on real data");
            }
            logger.info("=====================================");
            initialized = true;
        }
    }

    /**
     * Envoyer une requête au chatbot Gemini
     */
    public String chat(String userMessage, String userId) {
        // Log l'initialisation au premier appel
        logInitialization();
        
        logger.info("=== CHAT REQUEST START ===");
        logger.info("User: {}", userId);
        logger.info("Message: {}", userMessage);
        logger.info("API Key configured: {}", (geminiApiKey != null && !geminiApiKey.isEmpty()) ? "YES" : "NO");
        
        try {
            // Préparer le contexte avec les données qualité
            logger.info("Preparing context with quality data...");
            String context = prepareContext(userId);
            logger.info("Context prepared successfully (length: {} chars)", context.length());

            // Construire le prompt complet
            logger.info("Building full prompt...");
            String fullPrompt = buildPrompt(context, userMessage);
            logger.info("Prompt built successfully (length: {} chars)", fullPrompt.length());

            // Appeler l'API Gemini
            logger.info("Calling Gemini API...");
            String response = callGeminiApi(fullPrompt);
            logger.info("Gemini API response received (length: {} chars)", response.length());

            logger.info("=== CHAT REQUEST SUCCESS ===");
            return response;

        } catch (Exception e) {
            logger.error("=== CHAT REQUEST FAILED ===");
            logger.error("Error in chat service: ", e);
            return "Désolé, je rencontre un problème technique. Veuillez réessayer dans quelques instants.";
        }
    }

    /**
     * Préparer le contexte avec les données qualité
     */
    private String prepareContext(String userId) {
        StringBuilder context = new StringBuilder();

        try {
            // Récupérer les fiches qualité
            List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
            
            // Statistiques générales
            context.append("=== DONNÉES SYSTÈME QUALITÉ ===\n\n");
            context.append("Total de fiches qualité: ").append(fichesQualite.size()).append("\n");

            if (!fichesQualite.isEmpty()) {
                // Statistiques par statut
                Map<String, Long> parStatut = fichesQualite.stream()
                    .collect(Collectors.groupingBy(
                        f -> f.getStatut() != null ? f.getStatut() : "Non défini",
                        Collectors.counting()
                    ));
                
                context.append("\nRépartition par statut:\n");
                parStatut.forEach((statut, count) -> 
                    context.append("  - ").append(statut).append(": ").append(count).append("\n")
                );

                // Fiches en retard
                LocalDate today = LocalDate.now();
                List<FicheQualite> fichesEnRetard = fichesQualite.stream()
                    .filter(f -> f.getDateEcheance() != null && 
                                 f.getDateEcheance().isBefore(today) &&
                                 !"TERMINE".equals(f.getStatut()))
                    .collect(Collectors.toList());

                if (!fichesEnRetard.isEmpty()) {
                    context.append("\nFiches en retard (").append(fichesEnRetard.size()).append("):\n");
                    fichesEnRetard.stream().limit(5).forEach(f -> 
                        context.append("  - ").append(f.getTitre())
                               .append(" (Responsable: ").append(maskEmail(f.getResponsable()))
                               .append(", Échéance: ").append(f.getDateEcheance())
                               .append(")\n")
                    );
                }

                // Fiches par priorité
                Map<String, Long> parPriorite = fichesQualite.stream()
                    .filter(f -> f.getPriorite() != null)
                    .collect(Collectors.groupingBy(FicheQualite::getPriorite, Collectors.counting()));
                
                if (!parPriorite.isEmpty()) {
                    context.append("\nRépartition par priorité:\n");
                    parPriorite.forEach((priorite, count) -> 
                        context.append("  - ").append(priorite).append(": ").append(count).append("\n")
                    );
                }
            }

            // Récupérer les fiches de suivi
            List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
            context.append("\nTotal de fiches de suivi: ").append(fichesSuivi.size()).append("\n");

            if (!fichesSuivi.isEmpty()) {
                // Calculer les KPI moyens
                OptionalDouble tauxConformiteMoyen = fichesSuivi.stream()
                    .filter(f -> f.getTauxConformite() != null)
                    .mapToDouble(FicheSuivi::getTauxConformite)
                    .average();

                OptionalDouble delaiMoyen = fichesSuivi.stream()
                    .filter(f -> f.getDelaiTraitementJours() != null)
                    .mapToDouble(FicheSuivi::getDelaiTraitementJours)
                    .average();

                if (tauxConformiteMoyen.isPresent()) {
                    context.append("Taux de conformité moyen: ")
                           .append(String.format("%.1f%%", tauxConformiteMoyen.getAsDouble()))
                           .append("\n");
                }

                if (delaiMoyen.isPresent()) {
                    context.append("Délai de traitement moyen: ")
                           .append(String.format("%.1f jours", delaiMoyen.getAsDouble()))
                           .append("\n");
                }

                // Problèmes récurrents
                Map<String, Long> problemes = fichesSuivi.stream()
                    .filter(f -> f.getProblemes() != null && !f.getProblemes().isEmpty())
                    .collect(Collectors.groupingBy(FicheSuivi::getProblemes, Collectors.counting()));

                if (!problemes.isEmpty()) {
                    context.append("\nProblèmes identifiés:\n");
                    problemes.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .forEach(entry -> 
                            context.append("  - ").append(entry.getKey())
                                   .append(" (").append(entry.getValue()).append(" fois)\n")
                        );
                }
            }

            // Récupérer toutes les notifications pour statistiques générales
            List<Notification> notifications = notificationRepository.findAll();
            if (!notifications.isEmpty()) {
                long notificationsNonLues = notifications.stream()
                    .filter(n -> !n.isLu())
                    .count();
                if (notificationsNonLues > 0) {
                    context.append("\nNotifications non lues: ").append(notificationsNonLues).append("\n");
                }
            }

        } catch (Exception e) {
            logger.error("Error preparing context: ", e);
            context.append("Erreur lors de la récupération des données.\n");
        }

        return context.toString();
    }

    /**
     * Construire le prompt complet pour Gemini
     */
    private String buildPrompt(String context, String userMessage) {
        return "Tu es un assistant qualité intelligent et conversationnel nommé 'Assistant Qualité'. " +
               "Tu interagis de manière naturelle avec les utilisateurs comme un vrai chatbot.\n\n" +
               "PERSONNALITÉ:\n" +
               "- Amical, professionnel et serviable\n" +
               "- Tu réponds de manière conversationnelle et naturelle\n" +
               "- Tu poses des questions de clarification si nécessaire\n" +
               "- Tu t'adaptes au ton de l'utilisateur (formel ou informel)\n\n" +
               "RÈGLES DE CONVERSATION:\n" +
               "1. Réponds TOUJOURS en français\n" +
               "2. Sois conversationnel - pas de réponses robotiques ou templates\n" +
               "3. Si l'utilisateur dit 'bonjour', réponds naturellement avec un accueil personnalisé\n" +
               "4. Base tes réponses sur les données du contexte quand c'est pertinent\n" +
               "5. Sois concis (150-250 mots maximum)\n" +
               "6. Utilise des emojis occasionnellement pour être plus humain\n" +
               "7. Si tu ne comprends pas, demande des précisions\n\n" +
               "CONTEXTE DES DONNÉES:\n" + context + "\n\n" +
               "MESSAGE DE L'UTILISATEUR: " + userMessage + "\n\n" +
               "Réponds de manière naturelle et conversationnelle:";
    }

    /**
     * Appeler l'API Gemini avec retry intelligent
     */
    private String callGeminiApi(String prompt) {
        // Vérifier la clé API
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            logger.error("❌ GEMINI API KEY NOT CONFIGURED!");
            return "Désolé, le service d'IA n'est pas configuré. Veuillez contacter l'administrateur.";
        }

        logger.info("✓ Calling Gemini API with model: gemini-1.5-pro");
        
        // Construire l'URL avec la clé API
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        // Construire le corps de la requête
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));

        // Configuration de génération pour des réponses conversationnelles
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.9); // Plus créatif et conversationnel
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 800);
        requestBody.put("generationConfig", generationConfig);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Appel API avec retry intelligent
        int retryCount = 0;
        long waitTime = retryDelay;

        while (retryCount <= maxRetries) {
            try {
                logger.info("🔄 Attempt {}/{} - Calling Gemini API...", retryCount + 1, maxRetries + 1);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("✅ Gemini API call successful!");
                    String parsedResponse = parseGeminiResponse(response.getBody());
                    logger.info("✅ Response parsed successfully (length: {} chars)", parsedResponse.length());
                    return parsedResponse;
                }
                
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    logger.error("❌ Max retries reached. Quota exceeded.");
                    return "Désolé, le service est temporairement surchargé. Veuillez réessayer dans quelques minutes. " +
                           "Si le problème persiste, contactez l'administrateur pour vérifier le quota API.";
                }
                
                logger.warn("⚠️ Rate limit hit (429). Waiting {} ms before retry {}/{}...", 
                           waitTime, retryCount, maxRetries);
                
                try {
                    Thread.sleep(waitTime);
                    waitTime *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "Erreur lors de la tentative de reconnexion.";
                }
                
            } catch (Exception e) {
                retryCount++;
                logger.error("❌ Attempt {}/{} failed: {}", retryCount, maxRetries + 1, e.getMessage());
                
                if (retryCount > maxRetries) {
                    logger.error("❌ All retry attempts failed", e);
                    return "Désolé, je ne peux pas me connecter au service d'IA actuellement. " +
                           "Veuillez réessayer dans quelques instants.";
                }
                
                try {
                    Thread.sleep(waitTime);
                    waitTime *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "Erreur lors de la tentative de reconnexion.";
                }
            }
        }
        
        return "Désolé, le service est temporairement indisponible. Veuillez réessayer.";
    }

    /**
     * Parser la réponse de Gemini
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            return "Désolé, je n'ai pas pu générer une réponse appropriée.";
            
        } catch (Exception e) {
            logger.error("Error parsing Gemini response: ", e);
            return "Erreur lors du traitement de la réponse.";
        }
    }



    /**
     * Masquer les emails pour la confidentialité
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        return email.substring(0, 3) + "***";
    }
}
