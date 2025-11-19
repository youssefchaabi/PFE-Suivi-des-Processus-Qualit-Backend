package com.pfe.qualite.backend.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb+srv://youssef:yEgbkuPmWmpyhmYC@cluster0.dqkhj5p.mongodb.net/qualitydb}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "qualitydb";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        try {
            // Validation de l'URI
            if (mongoUri == null || mongoUri.trim().isEmpty() || 
                (!mongoUri.startsWith("mongodb://") && !mongoUri.startsWith("mongodb+srv://"))) {
                // URI par défaut si invalide
                mongoUri = "mongodb+srv://youssef:yEgbkuPmWmpyhmYC@cluster0.dqkhj5p.mongodb.net/qualitydb";
                System.out.println("⚠️ URI MongoDB invalide, utilisation de l'URI par défaut");
            }
            
            System.out.println("✅ Connexion MongoDB avec URI: " + mongoUri.replaceAll(":[^:@]+@", ":****@"));
            
            ConnectionString connectionString = new ConnectionString(mongoUri);
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            
            return MongoClients.create(mongoClientSettings);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du client MongoDB: " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter à MongoDB", e);
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
