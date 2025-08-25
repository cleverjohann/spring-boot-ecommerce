package com.example.springbootecommerce.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de refresh de token JWT.
 * Contiene el token de refresh necesario para generar un nuevo token de acceso.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    @NotBlank(message = "El token de refresh es obligatorio")
    @JsonProperty("refresh_token")
    private String refreshToken;

}
