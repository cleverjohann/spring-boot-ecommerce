package com.example.springbootecommerce.auth.service;


import com.example.springbootecommerce.auth.dto.*;

/**
 * Interfaz del servicio de autenticación que define las operaciones de negocio.
 * Sigue el principio de segregación de interfaces (ISP).
 */
public interface AuthService {

    // ========================================================================
    // OPERACIONES DE AUTENTICACIÓN
    // ========================================================================

    /**
     * Auténtica un usuario con email y contraseña
     *
     * @param loginRequest Datos de login
     * @return Respuesta JWT con tokens y información del usuario
     */
    JwtResponseDTO login(LoginRequestDTO loginRequest);

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param registerRequest Datos de registro
     * @return Respuesta JWT con tokens y información del usuario creado
     */
    JwtResponseDTO register(RegisterRequestDTO registerRequest);

    /**
     * Actualiza el token de acceso usando un refresh token
     *
     * @param refreshRequest Solicitud con refresh token
     * @return Nueva respuesta JWT con token actualizado
     */
    JwtResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest);

    /**
     * Cierra sesión invalidando los tokens
     *
     * @param token Token de acceso a invalidar
     * @return true si se cerró sesión correctamente
     */
    boolean logout(String token);

    // ========================================================================
    // GESTIÓN DE CONTRASEÑAS
    // ========================================================================

    /**
     * Cambia la contraseña del usuario autenticado
     *
     * @param changePasswordDTO Datos del cambio de contraseña
     * @return true si se cambió correctamente
     */
    boolean changePassword(ChangePasswordDTO changePasswordDTO);

    /**
     * Inicia el proceso de restablecimiento de contraseña
     *
     * @param resetRequest Solicitud con email del usuario
     * @return true si se envió el email de restablecimiento
     */
    boolean requestPasswordReset(PasswordResetRequestDTO resetRequest);

    /**
     * Restablece la contraseña usando un token de reset
     *
     * @param token Token de restablecimiento
     * @param newPassword Nueva contraseña
     * @return true si se restableció correctamente
     */
    boolean resetPassword(String token, String newPassword);

    // ========================================================================
    // VALIDACIONES Y UTILIDADES
    // ========================================================================

    /**
     * Valida si un token es válido y no ha expirado
     *
     * @param token Token a validar
     * @return true si es válido
     */
    boolean isTokenValid(String token);

    /**
     * Válida si un email está disponible para registro
     *
     * @param email Email a validar
     * @return true si está disponible
     */
    boolean isEmailAvailable(String email);

    /**
     * Obtiene información del usuario desde un token válido
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    String getUserEmailFromToken(String token);
}
