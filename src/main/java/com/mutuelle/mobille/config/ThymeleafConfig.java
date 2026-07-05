package com.mutuelle.mobille.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        // Ajout du dialecte Layout
        templateEngine.addDialect(new LayoutDialect());

        return templateEngine;
    }
}
