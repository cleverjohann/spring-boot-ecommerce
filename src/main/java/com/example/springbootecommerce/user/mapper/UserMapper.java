package com.example.springbootecommerce.user.mapper;

import com.example.springbootecommerce.user.dto.AddressDTO;
import com.example.springbootecommerce.user.dto.UpdateUserDTO;
import com.example.springbootecommerce.user.dto.UserDTO;
import com.example.springbootecommerce.user.entity.Address;
import com.example.springbootecommerce.user.entity.Role;
import com.example.springbootecommerce.user.entity.User;
import jdk.jfr.Name;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre entidades User y DTOs usando MapStruct.
 * Sigue el principio de responsabilidad única y facilita el mantenimiento.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AddressMapper.class}
)
public interface UserMapper {

    // ========================================================================
    // CONVERSIONES BÁSICAS USER -> DTO
    // ========================================================================
    /**
     * Convierte User entity a UserDTO básico
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "registrationDate", source = "createdAt")
    @Mapping(target = "lastUpdateDate", source = "updatedAt")
    @Mapping(target = "defaultAddress", source = "java(findDefaultAddress(user.getAddresses()))")
    UserDTO toUserDTO(User user);

    /**
     * Convierte lista de Users a lista de UserDTOs
     */
    List<UserDTO> toUserDTOs(List<User> users);

    /**
     * Convierte User a UserSummaryDTO para listados
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "registrationDate", source = "createdAt")
    @Mapping(target = "primaryRole", source = "java(getPrimaryRole(user.getRoles()))")
    @Mapping(target = "orderCount", ignore = true)
    UserDTO.UserSummaryDTO toUserSummaryDTO(User user);

    /**
     * Convierte User a PublicUserDTO (información mínima)
     */
    @Mapping(target = "displayName", expression = "java(user.getFirstName())")
    @Mapping(target = "memberSince", source = "createdAt")
    @Mapping(target = "reviewCount", ignore = true)
    UserDTO.PublicProfileDTO toPublicProfileDTO(User user);

    // ========================================================================
    // CONVERSIONES DTO -> ENTITY (para actualizaciones)
    // ========================================================================

    /**
     * Actualiza entidad User desde UpdateUserDTO
     * Solo actualiza campos no nulos
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) //Email se maneja por separado por seguridad
    @Mapping(target = "passwordHash", ignore = true) //Contraseña se maneja por separado
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateUserFromDTO(UpdateUserDTO updateUserDTO, @MappingTarget User user);

    // ========================================================================
    // MÉTODOS AUXILIARES (default methods)
    // ========================================================================

    /**
     * Convierte Set de Roles a Set de Strings
     */
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Encuentra la dirección por defecto
     */
    default AddressDTO findDefaultAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        return addresses.stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .map(this::addressToAddressDTO)
                .orElse(null);
    }

    /**
     * Obtiene el rol principal (el primero en orden alfabético)
     */
    default String getPrimaryRole(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    /**
     * Conversión simple de Address a AddressDTO
     * (se usa en métodos auxiliares)
     */
    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    AddressDTO addressToAddressDTO(Address address);

    // ========================================================================
    // MAPPERS ESPECIALIZADOS PARA DIFERENTES CONTEXTOS
    // ========================================================================

    /**
     * Mapper para contexto administrativo (incluye más detalles)
     */
    @Named("adminView")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles))")
    @Mapping(target = "registrationDate", source = "createdAt")
    @Mapping(target = "lastUpdateDate", source = "updatedAt")
    @Mapping(target = "defaultAddress", source = "java(findDefaultAddress(user.getAddresses()))")
    @Mapping(target = "address", source = "addresses")
    UserDTO toAdminUserDTO(User user);

    /**
     * Mapper para respuesta después de login (información básica + roles)
     */
    @Name("loginResponse")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "registrationDate", source = "createdAt")
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    @Mapping(target = "totalOrders",ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "totalReview", ignore = true)
    UserDTO toLoginResponseDTO(User user);

    // ========================================================================
    // MAPPERS PARA CONSTRUCCIÓN DE ENTIDADES
    // ========================================================================
    /**
     * Crea un User básico (para registro)
     * No incluye relaciones complejas
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) //Contraseña se maneja por separado
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "roles", ignore = true) //Roles se asignan después
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User createUserFromBasicInfo(String firstName, String lastName, String email);

    // ========================================================================
    // MAPPERS CON CONFIGURACIÓN PERSONALIZADA
    // ========================================================================
    /**
     * Mapper que incluye estadísticas (para dashboards)
     */
    default void enrichWithStatistics(@MappingTarget UserDTO userDTO,User user) {
        //Esta función se puede usar para agregar estadísticas calculadas
        //después del mapeo principal, si es necesario.
        if (userDTO.getTotalOrders() == null) {
            userDTO.setTotalOrders(0);
        }
        if (userDTO.getTotalSpent() == null) {
            userDTO.setTotalSpent(0.0);
        }
        if (userDTO.getTotalReview() == null) {
            userDTO.setTotalReview(0);
        }
    }

    // ========================================================================
    // MAPPERS PARA VALIDACIÓN Y LOGGING
    // ========================================================================
    /**
     * Crea un resumen del usuario para logging (sin datos sensibles)
     */
    default String createUserSummaryForLogging(User user) {
        if (user == null) {
            return "null";
        }
        return String.format("User{id=%d, email='%s', active=%s, roles=%d}",
                user.getId(),
                user.getEmail(),
                user.getIsActive(),
                user.getRoles() != null ? user.getRoles().size() : 0);
    }


}
