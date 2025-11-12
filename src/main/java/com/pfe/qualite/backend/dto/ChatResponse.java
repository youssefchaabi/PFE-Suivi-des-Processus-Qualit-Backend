package com.pfe.qualite.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String message;
    private String type; // "success", "error", "info"
    private Date timestamp;
    private boolean isTyping;
}
