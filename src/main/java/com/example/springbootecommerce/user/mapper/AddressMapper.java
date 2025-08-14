package com.example.springbootecommerce.user.mapper;

import com.example.springbootecommerce.user.dto.AddressDTO;
import com.example.springbootecommerce.user.dto.CreatedAddressDTO;
import com.example.springbootecommerce.user.entity.Address;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para conversión entre entidades Address y DTOs usando MapStruct.
 * Maneja las conversiones bidireccionales y transformaciones de datos.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {
    // ========================================================================
    // CONVERSIONES ENTITY -> DTO
    // ========================================================================
    /**
     * Convierte Address entity a AddressDTO
     */
    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    AddressDTO toAddressDTO(Address address);

    /**
     * Convierte lista de Address entities a lista de AddressDTOs
     */
    List<AddressDTO> toAddressDTOs(List<Address> addresses);

    // ========================================================================
    // CONVERSIONES DTO -> ENTITY
    // ========================================================================

    /**
     * Convierte CreateAddressDTO a Address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Ignora el usuario, se asignará después
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Address toAddress(CreatedAddressDTO createdAddressDTO);

    /**
     * Convierte AddressDTO a Address entity (para actualizaciones)
     */
    @Mapping(target = "user",ignore = true )
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Address toAddress(AddressDTO addressDTO);

    // ========================================================================
    // ACTUALIZACIONES PARCIALES
    // ========================================================================
    /**
     * Actualiza una entidad Address existente desde CreateAddressDTO
     * Solo actualiza campos no nulos
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true )
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateAddressFromDTO(CreatedAddressDTO dto , @MappingTarget Address address);

    /**
     * Actualiza una entidad Address existente desde AddressDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateAddressFromDTO(AddressDTO addressDTO, @MappingTarget Address address);

    // ========================================================================
    // MÉTODOS AUXILIARES Y POST-PROCESAMIENTO
    // ========================================================================

    /**
     * Procesamiento después del mapeo para normalizar datos
     */
    @AfterMapping
    default void normalizeAddress(@MappingTarget Address address){
        if (address.getStreet() != null){
            address.setStreet(address.getStreet().trim());
        }
        if (address.getCity() != null){
            address.setCity(capitalizeWords(address.getCity().trim()));
        }
        if (address.getState() != null){
            address.setState(capitalizeWords(address.getState().trim()));
        }
        if (address.getPostalCode() != null){
            address.setPostalCode(address.getPostalCode().trim().toUpperCase());
        }
        if (address.getCountry() != null){
            address.setCountry(capitalizeWords(address.getCountry().trim()));
        }
        if (address.getCompany() != null){
            address.setCompany(address.getCompany().trim());
        }
        if (address.getPhoneNumber() != null){
            address.setPhoneNumber(address.getPhoneNumber().trim());
        }
        if (address.getIsDefault() == null){
            address.setIsDefault(false);
        }
    }

    /**
     * Procesamiento después del mapeo para DTOs
     */
    @AfterMapping
    default void enrichAddressDTO(@MappingTarget AddressDTO dto, Address address) {
        //Asegurar que fullAddress se calcule correctamente
        if (dto.getFullAddress() == null && address != null) {
            dto.setFullAddress(address.getFullAddress());
        }
    }

    // ========================================================================
    // MAPPERS ESPECIALIZADOS
    // ========================================================================
    /**
     * Mapper para dirección simplificada (solo información básica)
     */
    @Named("simplified")
    @Mapping(target = "fullAddress",expression = "java(address.getFullAddress())")
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "additionalInfo", ignore = true)
    AddressDTO toSimplifiedAddressDTO(Address address);

    /**
     * Mapper para dirección de envío (incluye toda la información relevante)
     */
    @Named("shipping")
    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    AddressDTO toShippingAddressDTO(Address address);

    /**
     * Convierte Address a un resumen string para logging
     */
    default String toAddressSummary(Address address) {
        if (address == null){
            return  null;
        }
        return String.format("Address{id=%d, city='%s', country='%s', default=%s}",
                address.getId(),
                address.getCity(),
                address.getCountry(),
                address.getIsDefault());
    }

    // ========================================================================
    // MÉTODOS UTILITARIOS
    // ========================================================================

    /**
     * Capitaliza la primera letra de cada palabra
     */
    default String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split("\\s+");
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

    /**
     * Crea una dirección básica desde parámetros individuales
     */
    default Address createBasicAddress(String street, String city, String state,
                                       String postalCode, String country) {
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountry(country);
        address.setIsDefault(false);

        // Normalizar datos
        normalizeAddress(address);

        return address;
    }

    /**
     * Crea AddressDTO desde parámetros básicos
     */
    default AddressDTO createBasicAddressDTO(String street, String city, String state,
                                             String postalCode, String country) {
        return AddressDTO.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country)
                .isDefault(false)
                .fullAddress(String.format("%s, %s, %s %s, %s",
                        street, city, state, postalCode, country))
                .build();
    }

}
