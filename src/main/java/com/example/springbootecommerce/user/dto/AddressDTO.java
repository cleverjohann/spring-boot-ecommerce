package com.example.springbootecommerce.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir información de direcciones.
 * Usado tanto para recibir datos del cliente como para enviar respuestas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Información de direcciones del usuario")
public class AddressDTO {

    @Schema(description = "ID único de la dirección", example = "1")
    private Long id;

    @Schema(description = "Calle y número de la dirección", example = "Av. Siempre Viva 742")
    @NotBlank(message = "La calle y número son obligatorios")
    @Size(max = 255, message = "La calle y número no pueden exceder los 255 caracteres")
    private String street;

    @Schema(description = "Número de apartamento o unidad", example = "Apt. 1B")
    @Size(max = 50, message = "El número de apartamento no puede exceder los 50 caracteres")
    private String apartmentNumber;

    @Schema(description = "Ciudad de la dirección", example = "Lima")
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
    private String city;

    @Schema(description = "Estado o provincia de la dirección", example = "Lima")
    @NotBlank(message = "El estado o provincia es obligatorio")
    @Size(max = 100, message = "El estado o provincia no puede exceder los 100 caracteres")
    private String state;

    @Schema(description = "Código postal de la dirección", example = "15001")
    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 20, message = "El código postal no puede exceder los 20 caracteres")
    private String postalCode;

    @Schema(description = "País de la dirección", example = "Perú")
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede exceder los 100 caracteres")
    private String country;

    @Schema(description = "Nombre de la empresa asociada a la dirección", example = "Mi Empresa S.A.")
    @Size(max = 100, message = "El nombre de la empresa no puede exceder los 100 caracteres")
    private String company;

    @Schema(description = "Número de teléfono asociado a la dirección", example = "+51 987654321")
    @Size(max = 20, message = "El número de teléfono no puede exceder los 20 caracteres")
    private String phoneNumber;

    @Schema(description = "Información adicional sobre la dirección", example = "Entrada por la puerta trasera")
    @Size(max = 500, message = "La información adicional no puede exceder los 500 caracteres")
    private String additionalInfo;

    @Schema(description = "Indica si esta es la dirección por defecto del usuario", example = "true")
    private Boolean isDefault;

    @Schema(description = "Dirección completa formateada",
            example = "Av. Siempre Viva 742, Apt. 1B, Lima, Lima, 15001, Perú")
    private String fullAddress;


    // ========================================================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================================================

    public boolean isDefaultAddress() {
        return isDefault != null && isDefault;
    }

    public boolean isBusinessAddress() {
        return company != null && !company.isBlank();
    }

    public boolean hasContactNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    /**
     * Verifica si la dirección está completa (campos obligatorios)
     */
    public boolean isComplete() {
        return street != null && !street.isBlank() &&
               city != null && !city.isBlank() &&
               state != null && !state.isBlank() &&
               postalCode != null && !postalCode.isBlank() &&
               country != null && !country.isBlank();
    }

    // ========================================================================
    // BUILDERS ESPECÍFICOS
    // ========================================================================

    public static AddressDTOBuilder basicAddress(String street, String city,
                                                 String state, String postalCode, String country) {
        return AddressDTO.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country);
    }

    public static AddressDTOBuilder businessAddress(String company){
        return AddressDTO.builder()
                .company(company);
    }
}
