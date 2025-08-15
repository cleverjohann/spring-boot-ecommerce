package com.example.springbootecommerce.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO para cambio de contraseña de usuario autenticado.
 * Requiere la contraseña actual para validar la operación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @JsonProperty("current_password")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 50, message = "La nueva contraseña debe tener entre 6 y 50 caracteres")
    @JsonProperty("new_password")
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    @JsonProperty("confirm_password")
    private String confirmPassword;

}
