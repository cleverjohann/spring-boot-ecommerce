package com.example.springbootecommerce.config;

import com.example.springbootecommerce.shared.exception.CustomAccessDeniedHandler;
import com.example.springbootecommerce.shared.security.CustomUserDetailService;
import com.example.springbootecommerce.shared.security.JwtAuthenticationEntryPoint;
import com.example.springbootecommerce.shared.security.JwtRequestFilter;
import com.example.springbootecommerce.shared.security.JwtResponseFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración principal de seguridad de Spring Security.
 * Define la cadena de filtros, proveedores de autenticación y políticas de CORS.
 *
 * <p>Refactorizada para evitar dependencias circulares:</p>
 * <ul>
 *   <li>PasswordEncoder movido a PasswordEncoderConfig</li>
 *   <li>Dependencias simplificadas en filtros</li>
 *   <li>Inyección directa de beans requeridos</li>
 * </ul>
 *
 * @author Sistema de Seguridad
 * @version 1.1
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtResponseFilter jwtResponseFilter;
    private final PasswordEncoder passwordEncoder; // Inyectado desde PasswordEncoderConfig
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    // ========================================================================
    // CONFIGURACIÓN PRINCIPAL DE SEGURIDAD
    // ========================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF ya que usamos JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar manejo de excepciones
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                )

                // Política de sesiones: stateless (sin sesión)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configurar autorización de endpoints
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos (no requieren autenticación)
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/check-email"
                        ).permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // Documentación de API (Swagger/OpenAPI)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Endpoints de monitoreo (Actuator)
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Recursos estáticos
                        .requestMatchers(
                                "/favicon.ico",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**"
                        ).permitAll()

                        // Endpoints de usuarios - requieren autenticación
                        .requestMatchers("/api/v1/users/profile/**").authenticated()
                        .requestMatchers("/api/v1/users/addresses/**").authenticated()
                        .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")

                        // Endpoints de productos - acceso mixto
                        .requestMatchers("/api/v1/products").permitAll() // Listar productos
                        .requestMatchers("/api/v1/products/{id}").permitAll() // Ver producto específico
                        .requestMatchers("/api/v1/products/search").permitAll() // Buscar productos
                        .requestMatchers("/api/v1/products/category/**").permitAll() // Por categoría
                        .requestMatchers("/api/v1/products/admin/**").hasRole("ADMIN") // Gestión de productos

                        // Endpoints de categorías - acceso mixto
                        .requestMatchers("/api/v1/categories").permitAll() // Listar categorías
                        .requestMatchers("/api/v1/categories/{id}").permitAll() // Ver categoría
                        .requestMatchers("/api/v1/categories/admin/**").hasRole("ADMIN") // Gestión

                        // Endpoints de carrito de compras - requieren autenticación
                        .requestMatchers("/api/v1/cart/**").authenticated()

                        // Endpoints de órdenes - requieren autenticación
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/orders/admin/**").hasRole("ADMIN")

                        // Endpoints de reviews - acceso mixto
                        .requestMatchers("/api/v1/reviews/product/**").permitAll() // Ver reviews
                        .requestMatchers("/api/v1/reviews/create").authenticated() // Crear review
                        .requestMatchers("/api/v1/reviews/admin/**").hasRole("ADMIN")

                        // Endpoints de pagos - requieren autenticación
                        .requestMatchers("/api/v1/payments/**").authenticated()

                        // Por defecto, todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )

                // Configurar proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // Agregar filtros JWT
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtResponseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ========================================================================
    // BEANS DE CONFIGURACIÓN
    // ========================================================================

    /**
     * Proveedor de autenticación personalizado.
     * Utiliza el PasswordEncoder inyectado desde PasswordEncoderConfig.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder); // Usado desde inyección
        authProvider.setHideUserNotFoundExceptions(false); // Para debugging, cambiar a true en producción
        return authProvider;
    }

    /**
     * Manager de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configuración de CORS para permitir requests desde el frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos - configurar según el entorno
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",  // React dev server
                "http://localhost:4200",  // Angular dev server
                "http://localhost:8080",  // Vue dev server
                "https://mi-ecommerce.com", // Producción
                "https://*.mi-ecommerce.com" // Subdominios
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "X-API-Key"
        ));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "X-Token-Remaining-Time",
                "X-Response-Time"
        ));

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ========================================================================
    // CONFIGURACIONES ADICIONALES PARA DIFERENTES ENTORNOS
    // ========================================================================

    /**
     * Configuración de CORS para desarrollo (más permisiva)
     */
    @Bean
    public CorsConfigurationSource devCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ========================================================================
    // MÉTODOS DE UTILIDAD
    // ========================================================================

    /**
     * Endpoints que no requieren autenticación
     */
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/favicon.ico",
            "/error"
    };

    /**
     * Endpoints que requieren rol de administrador
     */
    public static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/users/admin/**",
            "/api/v1/products/admin/**",
            "/api/v1/categories/admin/**",
            "/api/v1/orders/admin/**",
            "/api/v1/reviews/admin/**",
            "/actuator/**"
    };

    /**
     * Endpoints de solo lectura (GET) que pueden ser públicos
     */
    public static final String[] READ_ONLY_PUBLIC_ENDPOINTS = {
            "/api/v1/products",
            "/api/v1/products/{id}",
            "/api/v1/products/search",
            "/api/v1/categories",
            "/api/v1/categories/{id}",
            "/api/v1/reviews/product/**"
    };


}
