package com.mutuelle.mobille.controller;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TemplatePreviewController {

    @GetMapping("/preview/email")
    public String previewEmailIndex(Model model) {
        model.addAttribute("templates", List.of("welcome", "password-reset", "invoice", "notification"));
        return "email/preview-index"; // Créez une page HTML pour lister les templates
    }

    @GetMapping("/preview/email/{template}")
    public String previewEmail(
            @PathVariable String template,
            @RequestParam(defaultValue = "Franky") String prenom,
            @RequestParam(defaultValue = "ADH-2026001") String numero,
            @RequestParam(defaultValue = "https://mutuelle-enspy.cm/activate?token=abc123") String link,
            Model model) {

        model.addAttribute("prenom", prenom);
        model.addAttribute("numeroAdherent", numero);
        model.addAttribute("activationLink", link);
        model.addAttribute("logoUrl", "https://api.dicebear.com/7.x/initials/svg?seed=Mutuelle+ENSPY&backgroundColor=0056b3&fontColor=ffffff&fontSize=35");

        return "email/" + template;
    }
}