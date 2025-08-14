package com.example.springbootecommerce.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear nuevas direcciones.
 * Incluye todas las validaciones necesarias para la entrada de datos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información de dirección para creación")
public class CreatedAddressDTO {

    @Schema(description = "Calle y numero", example = "Av. Libertador 1234")
    @NotBlank(message = "La calle y número son obligatorios")
    @Size(min = 5, max = 255, message = "La calle y número deben tener entre 5 y 255 caracteres")
    private String street;

    @Schema(description = "Número de apartamento o unidad", example = "Apt. 1B")
    @Size(max = 50, message = "El número de apartamento no puede exceder los 50 caracteres")
    private String apartmentNumber;

    @Schema(description = "Ciudad de la dirección", example = "Lima")
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(min = 2, max = 100, message = "La ciudad debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\s-]+$",
            message = "La ciudad solo puede contener letras, espacios y guiones")
    private String city;

    @Schema(description = "Estado o provincia de la dirección", example = "Lima")
    @NotBlank(message = "El estado o provincia es obligatorio")
    @Size(min = 2, max = 100, message = "El estado o provincia debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\\\s-]+$",
            message = "El estado o provincia solo puede contener letras, espacios y guiones")
    private String state;

    @Schema(description = "Código postal de la dirección", example = "15001")
    @NotBlank(message = "El código postal es obligatorio")
    @Size(min = 4, max = 20, message = "El código postal debe tener entre 4 y 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z\\\\s-]+$",
            message = "El código postal solo puede contener letras, números, espacios y guiones")
    private String postalCode;

    @Schema(description = "País de la dirección", example = "Perú")
    @NotBlank(message = "El país es obligatorio")
    @Size(min = 2, max = 100, message = "El país debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ\\\\s-]+$",
            message = "El país solo puede contener letras, espacios y guiones")
    private String country;

    @Schema(description = "Nombre de la empresa asociada a la dirección", example = "Mi Empresa S.A.")
    @Size(max = 100, message = "El nombre de la empresa no puede exceder los 100 caracteres")
    private String company;

    @Schema(description = "Número de teléfono asociado a la dirección", example = "+51 987654321")
    @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
    @Pattern(regexp = "^[+]?[0-9\\s()-]{7,20}$",
            message = "El número de teléfono debe ser un número válido con formato internacional opcional")
    private String phoneNumber;

    @Schema(description = "Información adicional sobre la dirección", example = "Entrada por la puerta trasera")
    @Size(max = 500, message = "La información adicional no puede exceder los 500 caracteres")
    private String additionalInfo;

    @Schema(description = "Indica si es la dirección por defecto del usuario", example = "false",defaultValue = "false")
    @Builder.Default
    private Boolean isDefault = false;

    // ========================================================================
    // MÉTODOS DE VALIDACIÓN PERSONALIZADA
    // ========================================================================

    public boolean isValid() {
        return street != null && !street.isBlank() &&
               city != null && !city.isBlank() &&
               state != null && !state.isBlank() &&
               postalCode != null && !postalCode.isBlank() &&
               country != null && !country.isBlank();
    }

    /**
     * Normaliza los datos de entrada (trim, capitalización, etc.)
     */
    public void normalize() {
        if (street != null) {
            street = street.trim();
        }
        if (apartmentNumber != null) {
            apartmentNumber = apartmentNumber.trim();
        }
        if (city != null) {
            city = capitalizeWords(city.trim());
        }
        if (state != null) {
            state = capitalizeWords(state.trim());
        }
        if (postalCode != null) {
            postalCode = postalCode.trim().toUpperCase();
        }
        if (country != null) {
            country = capitalizeWords(country.trim());
        }
        if (company != null) {
            company = company.trim();
        }
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.trim();
        }
        if (additionalInfo != null) {
            additionalInfo = additionalInfo.trim();
        }
        if (isDefault == null) {
            isDefault = false;
        }
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                      .append(words[i].substring(1));
            }
        }
        return result.toString();
    }

    public String getSummary(){
        return String.format("%s, %s, %s, %s, %s",
                street != null ? street : "N/A",
                city != null ? city : "N/A",
                state != null ? state : "N/A",
                postalCode != null ? postalCode : "N/A",
                country != null ? country : "N/A");
    }
}
