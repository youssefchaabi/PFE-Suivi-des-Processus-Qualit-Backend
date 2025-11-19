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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

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
                mongoUri = "mongodb+srv://youssef:yEgbkuPmWmpyhmYC@cluster0.dqkhj5p.mongodb.net/qualitydb";
                System.out.println("⚠️ URI MongoDB invalide, utilisation de l'URI par défaut");
            }
            
            System.out.println("✅ Connexion MongoDB avec URI: " + mongoUri.replaceAll(":[^:@]+@", ":****@"));
            
            // Configuration SSL pour accepter tous les certificats (nécessaire pour Render)
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            ConnectionString connectionString = new ConnectionString(mongoUri);
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.invalidHostNameAllowed(true);
                        builder.context(sslContext);
                    })
                    .applyToSocketSettings(builder -> 
                        builder.connectTimeout(30, TimeUnit.SECONDS)
                               .readTimeout(30, TimeUnit.SECONDS)
                    )
                    .applyToClusterSettings(builder -> 
                        builder.serverSelectionTimeout(30, TimeUnit.SECONDS)
                    )
                    .build();
            
            return MongoClients.create(mongoClientSettings);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du client MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de se connecter à MongoDB", e);
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
