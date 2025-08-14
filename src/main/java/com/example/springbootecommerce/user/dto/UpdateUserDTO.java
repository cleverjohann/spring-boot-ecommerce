package com.example.springbootecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar información del usuario.
 * Los campos son opcionales (nullable) para permitir actualizaciones parciales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información para actualizar el usuario")
public class UpdateUserDTO {
    @Schema(description = "Nombre del usuario", example = "Juan Carlos")
    @Size(min = 2, max = 100,message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s-]+$",
            message = "El nombre solo puede contener letras, espacios y guiones")
    private  String firstName;

    @Schema(description = "Apellido del usuario", example = "Pérez Gonzales")
    @Size(min = 2, max = 100,message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s-]+$",
            message = "El apellido solo puede contener letras, espacios y guiones")
    private String lastName;

    @Schema(description = "Correo electrónico del usuario", example = "nuevo.email@domain.com")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder los 255 caracteres")
    private String email;

    @Schema(description = "Nueva contraseña del usuario", example = "NuevaContraseña123")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
    private String newPassword;

    @Schema(description = "Confirmación de la nueva contraseña", example = "NuevaContraseña123")
    private String confirmPassword;

    @Schema(description = "Contraseña actual (requerida para cambios de email o contraseña)")
    private String currentPassword;

    // ========================================================================
    // MÉTODOS DE VALIDACIÓN
    // ========================================================================

    /**
     * Verifica si se está intentando cambiar la contraseña
     */
    public boolean isChangingPassword() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }

    /**
     * Verifica si se está intentando cambiar el email
     */
    public boolean isChangingEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Verifica si las contraseñas coinciden
     */
    public boolean passwordsMatch() {
        if (!isChangingPassword()){
            return true;
        }
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    /**
     * Verifica si se requiere la contraseña actual
     */
    public boolean requiresCurrentPassword() {
        return isChangingEmail() || isChangingPassword();
    }

    /**
     * Verifica si hay al menos un campo para actualizar
     */
    public boolean hasUpdates() {
        return (firstName != null && !firstName.trim().isEmpty()) ||
                (lastName != null && !lastName.trim().isEmpty()) ||
                isChangingEmail() ||
                isChangingPassword();
    }

    /**
     * Normaliza los datos de entrada
     */
    public void normalize() {
        if (firstName != null) {
            firstName = capitalizeWords(firstName.trim());
        }
        if (lastName != null) {
            lastName = capitalizeWords(lastName.trim());
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (newPassword != null) {
            newPassword = newPassword.trim();
        }
        if (confirmPassword != null) {
            confirmPassword = confirmPassword.trim();
        }
        if (currentPassword != null) {
            currentPassword = currentPassword.trim();
        }
    }

    /**
     * Capitaliza la primera letra de cada palabra
     */
    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Limpia datos sensibles después del procesamiento
     */
    public void clearSensitiveData() {
        newPassword = null;
        confirmPassword = null;
        currentPassword = null;
    }

    /**
     * Obtiene un resumen de los cambios para logging
     */
    public String getChangeSummary() {
        StringBuilder summary = new StringBuilder("Actualización de usuario: ");
        if (firstName != null && !firstName.trim().isEmpty()) {
            summary.append("Nombre: ").append(firstName).append(", ");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            summary.append("Apellido: ").append(lastName).append(", ");
        }
        if (isChangingEmail()) {
            summary.append("Email: ").append(email).append(", ");
        }
        if (isChangingPassword()) {
            summary.append("Contraseña: [cambiada], ");
        }
        String result = summary.toString();

        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    // ========================================================================
    // VALIDACIÓN CRUZADA
    // ========================================================================
    public boolean isValid() {
        // Si no hay actualizaciones, no es válido
        if (!hasUpdates()) {
            return false;
        }

        // Si cambia contraseña, debe coincidir con la confirmación
        if (isChangingPassword() && !passwordsMatch()) {
            return false;
        }

        // Si requiere contraseña actual, debe estar presente
        return !requiresCurrentPassword() ||
                (currentPassword != null && !currentPassword.trim().isEmpty());
    }

}
