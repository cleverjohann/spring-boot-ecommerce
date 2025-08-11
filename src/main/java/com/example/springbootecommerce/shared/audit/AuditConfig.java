package com.example.springbootecommerce.shared.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración de auditoría JPA.
 * Habilita el tracking automático de creación y modificación de entidades.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAwareImpl auditorProvider(){
        return new AuditorAwareImpl();
    }
}
