package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    private String message;
    private String utilisateurId;
    private Date dateCreation;
    private boolean lu;
    private String type;
    private String objetId;
    
    // Date du dernier email envoyé pour cette notification
    private Date dateDernierEmail;
}
