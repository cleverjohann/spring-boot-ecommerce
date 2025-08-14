package com.example.springbootecommerce.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO para transferir información del usuario.
 * Se usa en respuestas de la API (no incluye datos sensibles como password).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Información del usuario respuestas de la API")
public class UserDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Perez")
    private String lastName;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Nombre completo del usuario", example = "Juan Perez")
    private String fullName;

    @Schema(description = "Estado del usuario", example = "true")
    private Boolean isActive;

    @Schema(description = "Roles asignados al usuario")
    private Set<String> roles;

    @Schema(description = "Direcciones del usuario")
    private List<AddressDTO> addresses;

    @Schema(description = "Direcciones del usuario")
    private AddressDTO defaultAddress;

    @Schema(description = "Fecha de registro del usuario", example = "2021-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDate;

    @Schema(description = "Fecha de última actualización del usuario", example = "2021-01-15 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateDate;

    //Campos estadísticos opcionales (vistas administrativas)
    @Schema(description = "Número total de órdenes del usuario", example = "10")
    private Integer totalOrders;

    @Schema(description = "Total gastado por el usuario", example = "150.75")
    private Double totalSpent;

    @Schema(description = "Numero total de reseñas escritas", example = "5")
    private Integer totalReview;

    // ========================================================================
    // MÉTODOS DE CONVENIENCIA
    // ========================================================================

    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean hasAddress() {
        return addresses != null && !addresses.isEmpty();
    }

    public int getAddressCount() {
        return addresses != null ? addresses.size() : 0;
    }

    // ========================================================================
    // BUILDERS ESPECÍFICOS PARA DIFERENTES CONTEXTOS
    // ========================================================================

    /**
     * Builder para respuesta básica de usuario (sin datos estadísticos)
     */
    public static UserDTO.UserDTOBuilder basicUser(){
        return UserDTO.builder();
    }

    /**
     * Builder para respuesta detallada de usuario (con direcciones)
     */
    public static UserDTO.UserDTOBuilder detailedUser() {
        return UserDTO.builder();
    }

    /**
     * Builder para vista administrativa (con estadísticas)
     */
    public static UserDTO.UserDTOBuilder adminView(){
        return UserDTO.builder();
    }

    /**
     * DTO simplificado para listas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Información simplificada del usuario para listas")
    public static class UserSummaryDTO {

        @Schema(description = "ID único del usuario")
        private Long id;

        @Schema(description = "Nombre completo del usuario")
        private String fullName;

        @Schema(description = "Correo electrónico del usuario")
        private String email;

        @Schema(description = "Estado inactivo/activo del usuario")
        private Boolean isActive;

        @Schema(description = "Fecha de registro del usuario")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime registrationDate;

        @Schema(description = "Rol principal del usuario")
        private String primaryRole;

        @Schema(description = "Número total de órdenes del usuario")
        private String orderCount;
    }

    /**
     * DTO para perfil público (información mínima)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Información pública del usuario")
    public static class PublicProfileDTO {

        @Schema(description = "ID único del usuario")
        private Long id;

        @Schema(description = "Nombre para mostrar del usuario")
        private String displayName;

        @Schema(description = "Fecha desde que el usuario es miembro")
        @JsonFormat(pattern = "yyyy-MM")
        private LocalDateTime memberSince;

        @Schema(description = "Número de reseñas escritas por el usuario")
        private Integer reviewCount;
    }
}
