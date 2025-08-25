package com.example.springbootecommerce.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro que intercepta las respuestas para agregar información adicional sobre tokens JWT.
 * Útil para debugging, logging y monitoreo de tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtResponseFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Crear wrapper para capturar la respuesta
        CustomHttpServletResponseWrapper responseWrapper =
                new CustomHttpServletResponseWrapper(response);

        try {
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, responseWrapper);

            // Procesar respuesta después de que se complete la cadena
            processResponse(request, responseWrapper);

        } catch (Exception e) {
            log.error("Error en JwtResponseFilter: {}", e.getMessage());
            // En caso de error, continuar normalmente
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Procesa la respuesta para agregar headers informativos sobre JWT
     */
    private void processResponse(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Solo procesar respuestas de endpoints de autenticación
            String requestURI = request.getRequestURI();

            if (isAuthEndpoint(requestURI)) {
                addJwtInfoHeaders(request, response);
                logAuthActivity(request, response);
            }

            // Agregar headers de seguridad generales
            addSecurityHeaders(response);

        } catch (Exception e) {
            log.warn("Error procesando respuesta JWT: {}", e.getMessage());
        }
    }

    /**
     * Verifica si la URI corresponde a un endpoint de autenticación
     */
    private boolean isAuthEndpoint(String uri) {
        return uri != null && (
                uri.startsWith("/api/v1/auth/login") ||
                        uri.startsWith("/api/v1/auth/register") ||
                        uri.startsWith("/api/v1/auth/refresh-token") ||
                        uri.startsWith("/api/v1/auth/logout")
        );
    }

    /**
     * Agrega headers informativos sobre JWT a la respuesta
     */
    private void addJwtInfoHeaders(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Obtener token del header Authorization si existe
            String authHeader = request.getHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Agregar timestamp de la respuesta
            response.setHeader("X-Response-Time",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Si hay token, agregar información útil
            if (token != null && !token.isEmpty()) {
                addTokenInfoHeaders(response, token);
            }

            // Agregar información del servidor
            response.setHeader("X-API-Version", "v1");
            response.setHeader("X-Service", "SpringBoot-Ecommerce");

        } catch (Exception e) {
            log.debug("Error agregando headers JWT: {}", e.getMessage());
        }
    }

    /**
     * Agrega headers específicos sobre el token JWT
     */
    private void addTokenInfoHeaders(HttpServletResponse response, String token) {
        try {
            // Verificar si el token es válido antes de extraer información
            if (jwtService.isTokenExpired(token)) {
                response.setHeader("X-Token-Status", "EXPIRED");
                return;
            }

            // Obtener información del token
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String roles = jwtService.extractRoles(token);
            long remainingTime = jwtService.getTokenRemainingTime(token);

            // Agregar headers informativos (sin información sensible)
            response.setHeader("X-Token-Status", "VALID");
            response.setHeader("X-Token-User", username != null ? "***@" +
                    username.substring(username.indexOf('@')) : "unknown");
            response.setHeader("X-Token-Remaining-Time", String.valueOf(remainingTime / 1000)); // en segundos

            if (userId != null) {
                response.setHeader("X-User-ID", String.valueOf(userId));
            }

            if (roles != null) {
                response.setHeader("X-User-Roles", roles);
            }

            // Verificar si es refresh token
            if (jwtService.isRefreshToken(token)) {
                response.setHeader("X-Token-Type", "REFRESH");
            } else {
                response.setHeader("X-Token-Type", "ACCESS");
            }

        } catch (Exception e) {
            log.debug("Error extrayendo información del token: {}", e.getMessage());
            response.setHeader("X-Token-Status", "INVALID");
        }
    }

    /**
     * Agrega headers de seguridad generales
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        // Headers de seguridad estándar
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Solo agregar HSTS en producción y con HTTPS
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Cache control para endpoints de autenticación
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * Registra actividad de autenticación para auditoría
     */
    private void logAuthActivity(HttpServletRequest request, HttpServletResponse response) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            int statusCode = response.getStatus();

            // Log estructurado para auditoría
            Map<String, Object> auditLog = new HashMap<>();
            auditLog.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            auditLog.put("method", method);
            auditLog.put("uri", uri);
            auditLog.put("clientIp", clientIp);
            auditLog.put("statusCode", statusCode);
            auditLog.put("success", statusCode >= 200 && statusCode < 300);

            if (userAgent != null) {
                auditLog.put("userAgent", userAgent.length() > 200 ?
                        userAgent.substring(0, 200) + "..." : userAgent);
            }

            // Obtener información del usuario si está en el token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String username = jwtService.extractUsername(token);
                    if (username != null) {
                        auditLog.put("username", username);
                    }
                } catch (Exception e) {
                    // Ignorar errores de extracción de token para logging
                }
            }

            // Log diferenciado según el tipo de operación
            if (statusCode >= 200 && statusCode < 300) {
                log.info("AUTH_SUCCESS: {}", objectMapper.writeValueAsString(auditLog));
            } else if (statusCode == 401) {
                log.warn("AUTH_UNAUTHORIZED: {}", objectMapper.writeValueAsString(auditLog));
            } else if (statusCode >= 400) {
                log.warn("AUTH_ERROR: {}", objectMapper.writeValueAsString(auditLog));
            }

        } catch (Exception e) {
            log.error("Error registrando actividad de autenticación: {}", e.getMessage());
        }
    }

    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Verificar headers de proxy
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        String remoteAddr = request.getRemoteAddr();

        // En desarrollo, puede ser localhost
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }

        return remoteAddr;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // No filtrar recursos estáticos y endpoints de health
        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }

    // ========================================================================
    // WRAPPER PERSONALIZADO PARA CAPTURAR RESPUESTA
    // ========================================================================

    /**
     * Wrapper personalizado para interceptar y modificar respuestas HTTP
     */
    private static class CustomHttpServletResponseWrapper extends HttpServletResponseWrapper {

        public CustomHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        // Aquí se pueden agregar métodos para capturar el contenido de la respuesta
        // si fuera necesario en el futuro
    }

    // ========================================================================
    // MÉTODOS ADICIONALES PARA MONITOREO
    // ========================================================================

    /**
     * Verifica si la respuesta contiene información sensible
     */
    @SuppressWarnings("unused")
    private boolean containsSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        // Patrones que indican información sensible
        String[] sensitivePatterns = {
                "\"password\":",
                "\"token\":",
                "\"secret\":",
                "\"key\":",
                "\"credentials\":"
        };

        String lowerContent = content.toLowerCase();
        for (String pattern : sensitivePatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sanitiza contenido eliminando información sensible
     */
    @SuppressWarnings("unused")
    private String sanitizeContent(String content) {
        if (content == null) {
            return null;
        }

        // Reemplazar valores sensibles con placeholders
        return content
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"")
                .replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"")
                .replaceAll("\"key\"\\s*:\\s*\"[^\"]*\"", "\"key\":\"***\"");
    }

    /**
     * Genera un ID único para rastrear la request
     */
    @SuppressWarnings("unused")
    private String generateRequestId() {
        return "req_" + System.currentTimeMillis() + "_" +
                Thread.currentThread().getId();
    }

    /**
     * Calcula el tiempo de procesamiento de la request
     */
    @SuppressWarnings("unused")
    private long calculateProcessingTime(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
}
