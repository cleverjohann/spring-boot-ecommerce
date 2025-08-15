package com.example.springbootecommerce.auth.dto;

import com.example.springbootecommerce.user.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de autenticación JWT.
 * Contiene los tokens de acceso y refresh junto con información del usuario autenticado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {

    @NotBlank(message = "El token de acceso es obligatorio")
    @JsonProperty("access_token")
    private String accessToken;

    @NotBlank(message = "El token de refresco es obligatorio")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Builder.Default
    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;

    @NotNull(message = "El usuario es obligatorio")
    @JsonProperty("user")
    private UserDTO user;

    @Builder.Default
    @JsonProperty("issued_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedAt = LocalDateTime.now();

    @JsonProperty("scope")
    private String scope;

    // ========================================================================
    // MÉTODOS DE UTILIDAD
    // ========================================================================

    /**
     * Verifica si la respuesta contiene tokens válidos
     *
     * @return true si ambos tokens están presentes y no están vacíos
     */
    public boolean hasValidTokens() {
        return accessToken != null && !accessToken.trim().isEmpty()
                && refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * Obtiene el token formateado con el prefijo "Bearer "
     *
     * @return Token formateado para header Authorization
     */
    public String getFormattedToken() {
        return tokenType + " " + accessToken;
    }
    /**
     * Crea una respuesta exitosa con los parámetros básicos
     *
     * @param accessToken Token de acceso
     * @param refreshToken Token de refresh
     * @param user Información del usuario
     * @param expiresIn Tiempo de expiración en segundos
     * @return Instancia de JwtResponseDTO
     */
    public static JwtResponseDTO success(String accessToken, String refreshToken,
                                         UserDTO user, Long expiresIn) {
        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .issuedAt(LocalDateTime.now())
                .build();
    }

}
