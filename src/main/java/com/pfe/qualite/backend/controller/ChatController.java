package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.dto.ChatRequest;
import com.pfe.qualite.backend.dto.ChatResponse;
import com.pfe.qualite.backend.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private GeminiService geminiService;

    // Rate limiting simple (en production, utiliser Redis ou similaire)
    private final Map<String, Long> lastRequestTime = new HashMap<>();
    private static final long MIN_REQUEST_INTERVAL = 3000; // 3 secondes

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            // Validation
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ChatResponse.builder()
                        .message("Le message ne peut pas être vide.")
                        .type("error")
                        .timestamp(new Date())
                        .isTyping(false)
                        .build()
                );
            }

            if (request.getMessage().length() > 500) {
                return ResponseEntity.badRequest().body(
                    ChatResponse.builder()
                        .message("Le message est trop long (maximum 500 caractères).")
                        .type("error")
                        .timestamp(new Date())
                        .isTyping(false)
                        .build()
                );
            }

            // Rate limiting simple
            String userId = request.getUserId() != null ? request.getUserId() : "anonymous";
            Long lastTime = lastRequestTime.get(userId);
            long currentTime = System.currentTimeMillis();

            if (lastTime != null && (currentTime - lastTime) < MIN_REQUEST_INTERVAL) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    ChatResponse.builder()
                        .message("Trop de requêtes. Veuillez patienter quelques secondes.")
                        .type("error")
                        .timestamp(new Date())
                        .isTyping(false)
                        .build()
                );
            }

            lastRequestTime.put(userId, currentTime);

            // Appeler le service Gemini
            String response = geminiService.chat(request.getMessage(), userId);

            logger.info("Chat message processed for user: {}", userId);

            return ResponseEntity.ok(
                ChatResponse.builder()
                    .message(response)
                    .type("success")
                    .timestamp(new Date())
                    .isTyping(false)
                    .build()
            );

        } catch (Exception e) {
            logger.error("Error processing chat message: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ChatResponse.builder()
                    .message("Désolé, une erreur s'est produite. Veuillez réessayer.")
                    .type("error")
                    .timestamp(new Date())
                    .isTyping(false)
                    .build()
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "chat");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
}
