package com.mutuelle.mobille;

import com.mutuelle.mobille.service.notifications.config.EmailService;
import com.mutuelle.mobille.service.notifications.config.NotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class MutuelleMobilleApplication {

	private final EmailService emailService;
	private final NotificationService notificationService;

	@Autowired
	public MutuelleMobilleApplication(EmailService emailService, NotificationService notificationService) {
		this.emailService = emailService;
		this.notificationService = notificationService;
	}

	public static void main(String[] args) {
		SpringApplication.run(MutuelleMobilleApplication.class, args);
	}


//	/**
//	 * Test de l'envoi de notification Thymeleaf au démarrage
//	 */
//	@PostConstruct
//	public void sendTestEmailOnStartup() {
//		try {
//			// Préparation des variables pour Thymeleaf
//			Map<String, Object> testVars = new HashMap<>();
//			testVars.put("userName", "Admin Mutuelle");
//			testVars.put("startupTime", java.time.LocalDateTime.now().toString());
//
//			// Construction du DTO avec le Builder
//			NotificationRequestDto request = NotificationRequestDto.builder()
//					.email("pandoraanimp@gmail.com")
//					.title("Démarrage Backend Mutuelle")
//					.templateName(TemplateMailsName.WELCOME) // Nom du fichier : email/welcome.html
//					.variables(testVars)
//					.channels(Set.of(NotificationChannel.EMAIL,NotificationChannel.PUSH))
//					.message("Ceci est le message de fallback si le template échoue.")
//					.build();
//
//			notificationService.sendNotification(request);
//			System.out.println(" Test de notification de démarrage envoyé !");
//
//		} catch (Exception e) {
//			System.err.println(" Erreur lors du test de démarrage : " + e.getMessage());
//		}
//	}


//	/**
//	 * Envoyer un email de test une fois que l'application est complètement démarrée
//	 */
//	@PostConstruct
//	public void sendTestEmailOnStartup() {
//		try {
//			emailService.sendSimpleEmail(
//					"pandoraanimp@gmail.com",
//					"Test démarrage application Mutuelle Mobile",
//					"L'application backend Mutuelle Mobile vient de démarrer avec succès !\n\n" +
//							"Date/Heure : " + java.time.LocalDateTime.now() +
//							"\nEnvironnement : " + System.getenv("SPRING_PROFILES_ACTIVE")
//			);
//			System.out.println("Email de test envoyé avec succès");
//		} catch (Exception e) {
//			System.err.println("Échec envoi email de test : " + e.getMessage());
//			e.printStackTrace();
//		}
//	}

	/**
	 * Ping régulier vers l'API Render pour éviter la mise en veille (cold start)
	 */
	@PostConstruct
	public void startKeepAlivePinger() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			try {
				URL url = new URL("https://mutuelle-mobile-api-2025.onrender.com/api/config/current");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000); // 5 secondes max
				conn.setReadTimeout(5000);
				conn.connect();

				int code = conn.getResponseCode();
				System.out.println("Ping Render → code: " + code + " | " + java.time.LocalDateTime.now());

				conn.disconnect();
			} catch (Exception e) {
				System.err.println("Échec ping Render : " + e.getMessage());
			}
		}, 0, 12, TimeUnit.MINUTES); // Toutes les 12 minutes
	}
}