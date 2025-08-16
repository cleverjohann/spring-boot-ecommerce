package com.example.springbootecommerce.shared.security;

import com.example.springbootecommerce.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Punto de entrada personalizado para manejar errores de autenticación JWT.
 *
 * <p>Esta clase se encarga de:</p>
 * <ul>
 *   <li>Interceptar errores de autenticación antes de llegar al controlador</li>
 *   <li>Generar respuestas de error consistentes y detalladas</li>
 *   <li>Registrar intentos de acceso no autorizados para auditoría</li>
 *   <li>Proporcionar información útil sin exponer detalles de seguridad</li>
 * </ul>
 *
 * <p>Casos de uso comunes:</p>
 * <ul>
 *   <li>Token JWT expirado o inválido</li>
 *   <li>Header Authorization malformado o ausente</li>
 *   <li>Token con firma inválida</li>
 *   <li>Usuario deshabilitado o no encontrado</li>
 *   <li>Acceso a recursos protegidos sin autenticación</li>
 * </ul>
 *
 * @author Sistema de Seguridad
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    // ========================================================================
    // IMPLEMENTACIÓN DE AuthenticationEntryPoint
    // ========================================================================

    /**
     * Maneja errores de autenticación generando respuestas HTTP estructuradas.
     *
     * <p>Este método se ejecuta automáticamente cuando:</p>
     * <ul>
     *   <li>Un usuario no autenticado intenta acceder a un recurso protegido</li>
     *   <li>Un token JWT es inválido, expirado o malformado</li>
     *   <li>Falla la validación de credenciales</li>
     *   <li>El usuario está deshabilitado o bloqueado</li>
     * </ul>
     *
     * @param request Petición HTTP que causó el error de autenticación
     * @param response Respuesta HTTP donde se escribirá el error
     * @param authException Excepción de autenticación que se produjo
     * @throws IOException Si hay error al escribir la respuesta
     * @throws ServletException Si hay error en el servlet
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Obtener información de contexto para logging y respuesta
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String clientIP = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String authHeader = request.getHeader("Authorization");

        // Logging de seguridad para auditoría
        logSecurityEvent(authException, requestURI, method, clientIP, userAgent, authHeader);

        // Determinar el tipo específico de error de autenticación
        AuthenticationErrorInfo errorInfo = categorizeAuthenticationError(authException, authHeader);

        // Crear respuesta de error estructurada
        ErrorResponse errorResponse = createErrorResponse(
                errorInfo,
                requestURI,
                authException.getMessage()
        );

        // Configurar headers de respuesta HTTP
        configureResponseHeaders(response, errorInfo);

        // Serializar y enviar respuesta JSON
        writeJsonResponse(response, errorResponse);
    }

    // ========================================================================
    // MÉTODOS DE CATEGORIZACIÓN DE ERRORES
    // ========================================================================

    /**
     * Categoriza el tipo específico de error de autenticación.
     *
     * @param authException Excepción de autenticación
     * @param authHeader Header de autorización de la petición
     * @return Información categorizada del error
     */
    private AuthenticationErrorInfo categorizeAuthenticationError(
            AuthenticationException authException,
            String authHeader) {

        String exceptionClass = authException.getClass().getSimpleName();
        String message = authException.getMessage();

        // Token completamente ausente
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return new AuthenticationErrorInfo(
                    "MISSING_TOKEN",
                    "Token de acceso requerido",
                    "Para acceder a este recurso necesitas estar autenticado",
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_001"
            );
        }

        // Header Authorization malformado
        if (!authHeader.startsWith("Bearer ")) {
            return new AuthenticationErrorInfo(
                    "MALFORMED_TOKEN",
                    "Formato de token inválido",
                    "El header Authorization debe tener el formato 'Bearer <token>'",
                    HttpStatus.UNAUTHORIZED,
                    "AUTH_002"
            );
        }

        // Errores específicos de JWT
        if (message != null) {
            if (message.contains("expired") || message.contains("expirado")) {
                return new AuthenticationErrorInfo(
                        "EXPIRED_TOKEN",
                        "Token expirado",
                        "Tu sesión ha expirado, por favor inicia sesión nuevamente",
                        HttpStatus.UNAUTHORIZED,
                        "AUTH_003"
                );
            }

            if (message.contains("signature") || message.contains("firma")) {
                return new AuthenticationErrorInfo(
                        "INVALID_SIGNATURE",
                        "Token con firma inválida",
                        "El token proporcionado no es válido",
                        HttpStatus.UNAUTHORIZED,
                        "AUTH_004"
                );
            }

            if (message.contains("malformed") || message.contains("malformado")) {
                return new AuthenticationErrorInfo(
                        "MALFORMED_TOKEN",
                        "Token malformado",
                        "El formato del token no es válido",
                        HttpStatus.UNAUTHORIZED,
                        "AUTH_005"
                );
            }

            if (message.contains("disabled") || message.contains("deshabilitado")) {
                return new AuthenticationErrorInfo(
                        "ACCOUNT_DISABLED",
                        "Cuenta deshabilitada",
                        "Tu cuenta ha sido deshabilitada, contacta al administrador",
                        HttpStatus.FORBIDDEN,
                        "AUTH_006"
                );
            }
        }

        // Error genérico de autenticación
        return new AuthenticationErrorInfo(
                "AUTHENTICATION_FAILED",
                "Error de autenticación",
                "Credenciales inválidas o insuficientes",
                HttpStatus.UNAUTHORIZED,
                "AUTH_000"
        );
    }

    // ========================================================================
    // MÉTODOS DE CONSTRUCCIÓN DE RESPUESTAS
    // ========================================================================

    /**
     * Crea una respuesta de error estructurada.
     *
     * @param errorInfo Información categorizada del error
     * @param path Path de la petición que falló
     * @param originalMessage Mensaje original de la excepción
     * @return Respuesta de error estructurada
     */
    private ErrorResponse createErrorResponse(
            AuthenticationErrorInfo errorInfo,
            String path,
            String originalMessage) {

        ErrorResponse errorResponse = new ErrorResponse(
                errorInfo.getError(),
                errorInfo.getUserMessage(),
                errorInfo.getHttpStatus().value(),
                path
        );

        // En desarrollo, incluir más detalles para debugging
        if (isDevelopmentEnvironment()) {
            errorResponse.setTimestamp(LocalDateTime.now());
            // No incluimos el mensaje original por seguridad, solo en logs
        }

        return errorResponse;
    }

    /**
     * Configura headers de respuesta HTTP apropiados.
     *
     * @param response Respuesta HTTP
     * @param errorInfo Información del error
     */
    private void configureResponseHeaders(HttpServletResponse response,
                                          AuthenticationErrorInfo errorInfo) {

        // Status code HTTP
        response.setStatus(errorInfo.getHttpStatus().value());

        // Content type para JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Headers de seguridad
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Header personalizado con código de error interno
        response.setHeader("X-Auth-Error-Code", errorInfo.getErrorCode());

        // CORS headers básicos para permitir lectura del error

        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Escribe la respuesta JSON al cliente.
     *
     * @param response Respuesta HTTP
     * @param errorResponse Objeto de error a serializar
     * @throws IOException Si hay error al escribir la respuesta
     */
    private void writeJsonResponse(HttpServletResponse response,
                                   ErrorResponse errorResponse) throws IOException {

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Error al escribir respuesta de autenticación: {}", e.getMessage());
            // Fallback a respuesta simple si falla la serialización JSON
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"AUTHENTICATION_FAILED\"," +
                            "\"message\":\"Error de autenticación\",\"status\":401}"
            );
        }
    }

    // ========================================================================
    // MÉTODOS DE LOGGING Y AUDITORÍA
    // ========================================================================

    /**
     * Registra eventos de seguridad para auditoría.
     *
     * @param authException Excepción de autenticación
     * @param requestURI URI solicitado
     * @param method Método HTTP
     * @param clientIP IP del cliente
     * @param userAgent User agent del cliente
     * @param authHeader Header de autorización
     */
    private void logSecurityEvent(AuthenticationException authException,
                                  String requestURI,
                                  String method,
                                  String clientIP,
                                  String userAgent,
                                  String authHeader) {

        // Log básico para todos los casos
        log.warn("Acceso no autorizado: {} {} desde IP: {} - Error: {}",
                method, requestURI, clientIP, authException.getClass().getSimpleName());

        // Log detallado para debugging (solo en desarrollo)
        if (isDevelopmentEnvironment()) {
            log.debug("Detalles del error de autenticación: " +
                            "URI: {}, Method: {}, IP: {}, UserAgent: {}, AuthHeader: {}, Exception: {}",
                    requestURI, method, clientIP,
                    userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "null",
                    authHeader != null ? "Bearer ***" : "null",
                    authException.getMessage());
        }

        // Log específico para casos críticos de seguridad
        if (isSuspiciousActivity(requestURI, authHeader, clientIP)) {
            log.error("ALERTA DE SEGURIDAD: Posible intento malicioso desde IP: {} " +
                            "accediendo a: {} con token: {}",
                    clientIP, requestURI, authHeader != null ? "presente" : "ausente");
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Obtiene la dirección IP real del cliente, considerando proxies.
     *
     * @param request Petición HTTP
     * @return Dirección IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Verifica si la aplicación está en modo desarrollo.
     *
     * @return true si está en desarrollo
     */
    private boolean isDevelopmentEnvironment() {
        // Verificar por variable de entorno o system property
        String environment = System.getProperty("spring.profiles.active", "");
        return environment.contains("dev") || environment.contains("development");
    }

    /**
     * Detecta actividad sospechosa que podría indicar un intento malicioso.
     *
     * @param requestURI URI solicitado
     * @param authHeader Header de autorización
     * @param clientIP IP del cliente
     * @return true si la actividad es sospechosa
     */
    private boolean isSuspiciousActivity(String requestURI, String authHeader, String clientIP) {
        // Detectar intentos con tokens obviamente inválidos
        if (authHeader != null && authHeader.length() > 1000) {
            return true;
        }

        // Detectar accesos a endpoints administrativos sin token
        if (requestURI.contains("/admin/") && authHeader == null) {
            return true;
        }

        // Aquí se podrían agregar más reglas de detección
        return false;
    }

    // ========================================================================
    // CLASE INTERNA PARA INFORMACIÓN DE ERROR
    // ========================================================================

    /**
     * Clase auxiliar para encapsular información categorizada de errores de autenticación.
     */
    private static class AuthenticationErrorInfo {
        private final String error;
        private final String technicalMessage;
        private final String userMessage;
        private final HttpStatus httpStatus;
        private final String errorCode;

        public AuthenticationErrorInfo(String error, String technicalMessage,
                                       String userMessage, HttpStatus httpStatus, String errorCode) {
            this.error = error;
            this.technicalMessage = technicalMessage;
            this.userMessage = userMessage;
            this.httpStatus = httpStatus;
            this.errorCode = errorCode;
        }

        public String getError() { return error; }
        public String getTechnicalMessage() { return technicalMessage; }
        public String getUserMessage() { return userMessage; }
        public HttpStatus getHttpStatus() { return httpStatus; }
        public String getErrorCode() { return errorCode; }
    }
}