package com.pfe.qualite.backend;

import com.pfe.qualite.backend.service.NomenclatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initNomenclatures(NomenclatureService nomenclatureService) {
		return args -> {
			log.info("🚀 Initialisation des nomenclatures par défaut...");
			try {
				nomenclatureService.initializeDefaultNomenclatures();
				log.info("✅ Nomenclatures initialisées avec succès");
			} catch (Exception e) {
				log.error("❌ Erreur lors de l'initialisation des nomenclatures", e);
			}
		};
	}

}
