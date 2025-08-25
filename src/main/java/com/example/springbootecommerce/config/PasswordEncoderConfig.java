package com.example.springbootecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración independiente para el codificador de contraseñas.
 * Separada de SecurityConfig para evitar dependencias circulares.
 *
 * @author Sistema de Configuración
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class PasswordEncoderConfig {
    /**
      * Valor configurable para la fuerza de BCrypt.
      * Se puede establecer en application.properties como:
      * spring.security.password.strength=10
     */
    @Value("${security.password.strength:10}")
    private int strength;

    /**
     * Bean para el codificador de contraseñas usando BCrypt.
     * Configurado con strength 12 para balance entre seguridad y rendimiento.
     *
     * @return PasswordEncoder configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(strength);
    }

}