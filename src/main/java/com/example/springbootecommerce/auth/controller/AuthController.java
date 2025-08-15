package com.example.springbootecommerce.auth.controller;

import com.example.springbootecommerce.auth.dto.*;
import com.example.springbootecommerce.auth.service.AuthService;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones de autenticación y autorización.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización de usuarios")
public class AuthController {

    private final AuthService authService;

    // ========================================================================
    // ENDPOINTS DE AUTENTICACIÓN
    // ========================================================================

    /**
     * Auténtica un usuario con email y contraseña.
     *
     * @param loginRequest Credenciales del usuario
     * @param request Contexto de la petición HTTP
     * @return Respuesta con tokens JWT y datos del usuario
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario mediante email y contraseña, devolviendo tokens JWT para acceso a la API"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> login(
            @Parameter(description = "Credenciales de acceso del usuario", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {

        log.info("POST /api/v1/auth/login - Intento de login para: {}", loginRequest.getEmail());

        JwtResponseDTO response = authService.login(loginRequest);

        log.info("Login exitoso para usuario: {} desde IP: {}",
                loginRequest.getEmail(), getClientIpAddress(request));

        return ResponseEntity.ok(ApiResponse.success(response, "Autenticación exitosa"));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param registerRequest Datos del nuevo usuario
     * @param request Contexto de la petición HTTP
     * @return Respuesta con tokens JWT y datos del usuario registrado
     */
    @Operation(
            summary = "Registrar usuario",
            description = "Registra un nuevo usuario en el sistema y devuelve tokens JWT para acceso inmediato"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o email ya registrado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> register(
            @Parameter(description = "Datos de registro del nuevo usuario", required = true)
            @Valid @RequestBody RegisterRequestDTO registerRequest,
            HttpServletRequest request) {

        log.info("POST /api/v1/auth/register - Intento de registro para: {}", registerRequest.getEmail());

        JwtResponseDTO response = authService.register(registerRequest);

        log.info("Registro exitoso para usuario: {} desde IP: {}",
                registerRequest.getEmail(), getClientIpAddress(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Usuario registrado exitosamente"));
    }

    /**
     * Renueva el token de acceso usando un refresh token válido.
     *
     * @param refreshRequest Solicitud con refresh token
     * @param request Contexto de la petición HTTP
     * @return Respuesta con nuevo token de acceso
     */
    @Operation(
            summary = "Renovar token",
            description = "Genera un nuevo token de acceso utilizando un refresh token válido"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido o expirado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token no autorizado"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDTO>> refreshToken(
            @Parameter(description = "Solicitud con refresh token válido", required = true)
            @Valid @RequestBody RefreshTokenRequestDTO refreshRequest,
            HttpServletRequest request) {

        log.debug("POST /api/v1/auth/refresh - Solicitud de refresh token desde IP: {}",
                getClientIpAddress(request));

        JwtResponseDTO response = authService.refreshToken(refreshRequest);

        log.debug("Token renovado exitosamente desde IP: {}", getClientIpAddress(request));

        return ResponseEntity.ok(ApiResponse.success(response, "Token renovado exitosamente"));
    }

    /**
     * Cierra la sesión del usuario invalidando sus tokens.
     *
     * @param request Contexto de la petición HTTP
     * @return Confirmación de logout exitoso
     */
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida los tokens del usuario para cerrar la sesión de forma segura"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token no autorizado"
            )
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(HttpServletRequest request) {

        log.debug("POST /api/v1/auth/logout - Solicitud de logout desde IP: {}",
                getClientIpAddress(request));

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        boolean loggedOut = authService.logout(token);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión cerrada exitosamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        if (loggedOut) {
            log.info("Logout exitoso desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.ok(ApiResponse.success(response, "Sesión cerrada exitosamente"));
        } else {
            log.warn("Fallo en logout desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al cerrar sesión", request.getRequestURI()));
        }
    }

    // ========================================================================
    // ENDPOINTS DE GESTIÓN DE CONTRASEÑAS
    // ========================================================================

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * @param changePasswordDTO Datos del cambio de contraseña
     * @param request Contexto de la petición HTTP
     * @return Confirmación del cambio exitoso
     */
    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite al usuario autenticado cambiar su contraseña proporcionando la actual y la nueva"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o contraseña actual incorrecta"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token no autorizado"
            )
    })
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @Parameter(description = "Datos para cambio de contraseña", required = true)
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            HttpServletRequest request) {

        log.info("PUT /api/v1/auth/change-password - Solicitud de cambio de contraseña desde IP: {}",
                getClientIpAddress(request));

        boolean changed = authService.changePassword(changePasswordDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña actualizada exitosamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        if (changed) {
            log.info("Contraseña cambiada exitosamente desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.ok(ApiResponse.success(response, "Contraseña actualizada exitosamente"));
        } else {
            log.warn("Fallo en cambio de contraseña desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al cambiar contraseña", request.getRequestURI()));
        }
    }

    /**
     * Solicita el restablecimiento de contraseña enviando un email.
     *
     * @param resetRequest Solicitud con email del usuario
     * @param request Contexto de la petición HTTP
     * @return Confirmación de envío de email
     */
    @Operation(
            summary = "Solicitar restablecimiento de contraseña",
            description = "Inicia el proceso de restablecimiento de contraseña enviando un email con instrucciones"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Solicitud procesada (email enviado si existe)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestPasswordReset(
            @Parameter(description = "Email del usuario que solicita el reset", required = true)
            @Valid @RequestBody PasswordResetRequestDTO resetRequest,
            HttpServletRequest request) {

        log.info("POST /api/v1/auth/forgot-password - Solicitud de reset para: {} desde IP: {}",
                resetRequest.getEmail(), getClientIpAddress(request));

        boolean sent = authService.requestPasswordReset(resetRequest);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el email existe, recibirás instrucciones para restablecer tu contraseña");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        // Siempre retornamos éxito por seguridad (no revelar si el email existe)
        return ResponseEntity.ok(ApiResponse.success(response,
                "Solicitud procesada correctamente"));
    }

    /**
     * Restablece la contraseña usando un token de reset válido.
     *
     * @param resetDTO Datos del restablecimiento
     * @param request Contexto de la petición HTTP
     * @return Confirmación del restablecimiento exitoso
     */
    @Operation(
            summary = "Restablecer contraseña",
            description = "Completa el proceso de restablecimiento de contraseña usando un token válido"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Contraseña restablecida exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o datos incorrectos"
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @Parameter(description = "Datos de restablecimiento de contraseña", required = true)
            @Valid @RequestBody PasswordResetDTO resetDTO,
            HttpServletRequest request) {

        log.info("POST /api/v1/auth/reset-password - Solicitud de reset con token desde IP: {}",
                getClientIpAddress(request));

        // Validar que las contraseñas coincidan
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Las contraseñas no coinciden", request.getRequestURI()));
        }

        boolean reset = authService.resetPassword(resetDTO.getResetToken(), resetDTO.getNewPassword());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña restablecida exitosamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        if (reset) {
            log.info("Contraseña restablecida exitosamente desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.ok(ApiResponse.success(response, "Contraseña restablecida exitosamente"));
        } else {
            log.warn("Fallo en restablecimiento de contraseña desde IP: {}", getClientIpAddress(request));
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token inválido o expirado", request.getRequestURI()));
        }
    }

    // ========================================================================
    // ENDPOINTS DE VALIDACIÓN Y UTILIDADES
    // ========================================================================

    /**
     * Valida si un token JWT es válido y no ha expirado.
     *
     * @param request Contexto de la petición HTTP
     * @return Estado de validez del token
     */
    @Operation(
            summary = "Validar token",
            description = "Verifica si un token JWT es válido y no ha expirado"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token válido"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado"
            )
    })
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(HttpServletRequest request) {

        log.debug("GET /api/v1/auth/validate-token - Validación de token desde IP: {}",
                getClientIpAddress(request));

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        boolean isValid = authService.isTokenValid(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        if (token != null && isValid) {
            String userEmail = authService.getUserEmailFromToken(token);
            response.put("user", userEmail);
        }

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success(response, "Token válido"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token inválido o expirado", request.getRequestURI()));
        }
    }

    /**
     * Verifica si un email está disponible para registro.
     *
     * @param email Email a verificar
     * @param request Contexto de la petición HTTP
     * @return Disponibilidad del email
     */
    @Operation(
            summary = "Verificar disponibilidad de email",
            description = "Verifica si un email está disponible para registro de nuevo usuario"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email inválido"
            )
    })
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEmailAvailability(
            @Parameter(description = "Email a verificar", required = true)
            @RequestParam("email")
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "Formato de email inválido") String email,
            HttpServletRequest request) {

        log.debug("GET /api/v1/auth/check-email - Verificación de email: {} desde IP: {}",
                email, getClientIpAddress(request));

        boolean isAvailable = authService.isEmailAvailable(email);

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "Email disponible" : "Email ya registrado");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(ApiResponse.success(response, "Verificación completada"));
    }

    /**
     * Obtiene información básica del usuario desde el token.
     *
     * @param request Contexto de la petición HTTP
     * @return Información del usuario autenticado
     */
    @Operation(
            summary = "Obtener información del usuario",
            description = "Obtiene información básica del usuario autenticado desde el token JWT"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Información obtenida exitosamente"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token no autorizado"
            )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCurrentUser(HttpServletRequest request) {

        log.debug("GET /api/v1/auth/me - Solicitud de información de usuario desde IP: {}",
                getClientIpAddress(request));

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        String userEmail = authService.getUserEmailFromToken(token);

        Map<String, String> response = new HashMap<>();
        response.put("email", userEmail);
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        if (userEmail != null) {
            return ResponseEntity.ok(ApiResponse.success(response, "Información obtenida exitosamente"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token inválido", request.getRequestURI()));
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================

    /**
     * Obtiene la dirección IP del cliente, considerando proxies.
     *
     * @param request Contexto de la petición HTTP
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
}