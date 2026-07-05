package com.mutuelle.mobille.service.notifications.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur envoi email simple", e);
        }
    }

    // Envoi HTML avec pièces jointes et images inline
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody,
                              Map<String, Resource> inlineImages, // clé = contentId, valeur = image
                              Resource... attachments) { // pièces jointes

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            // Ajout images inline (ex: <img src="cid:logo"> dans le HTML)
            if (inlineImages != null) {
                for (Map.Entry<String, Resource> entry : inlineImages.entrySet()) {
                    helper.addInline(entry.getKey(), entry.getValue());
                }
            }

            // Ajout pièces jointes
            if (attachments != null) {
                for (Resource attachment : attachments) {
                    helper.addAttachment(attachment.getFilename(), attachment);
                }
            }

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur envoi email HTML", e);
        }
    }
}