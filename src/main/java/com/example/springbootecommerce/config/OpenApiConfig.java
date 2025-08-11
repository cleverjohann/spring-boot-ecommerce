package com.example.springbootecommerce.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI para documentación automática de la API.
 * Proporciona una interfaz Swagger UI accesible en /swagger-ui.html
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers());
    }

    private Info apiInfo(){
        return new Info()
                .title(applicationName + " API")
                .description("""
                        API REST para plataforma de E-commerce construida con Spring Boot.
                        
                                            ## Características principales:
                                            - Autenticación JWT stateless
                                            - Gestión de usuarios y roles
                                            - Catálogo de productos con búsqueda avanzada
                                            - Carrito de compras persistente
                                            - Procesamiento de órdenes transaccional
                                            - Integración con pasarelas de pago
                                            - Sistema de notificaciones por email
                        
                                            ## Autenticación:
                                            La mayoría de los endpoints requieren autenticación. Primero registra un usuario
                                            o inicia sesión para obtener un token JWT, luego inclúyelo en el header Authorization
                                            con el formato: `Bearer <tu_token>`
                        
                                            ## Roles disponibles:
                                            - **USER**: Puede gestionar su perfil, carrito y órdenes
                                            - **ADMIN**: Puede gestionar productos, categorías y todas las órdenes
                        """)
                .version("v1")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact(){
        return new Contact()
                .name("E-commerce")
                .url("https://github.com/cleverjohann/spring-boot-ecommerce")
                .email("mcleverjohann@gmail.com");
    }

    private License apiLicense(){
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }
    private List<Server> apiServers(){
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Servidor de Desarrollo Local"),
                new Server()
                        .url("https://api-qa.ecommerce.com")
                        .description("Servidor de QA"),
                new Server()
                        .url("https://api.ecommerce.com")
                        .description("Servidor de Producción")
        );
    }

}
