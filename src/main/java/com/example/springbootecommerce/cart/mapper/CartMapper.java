package com.example.springbootecommerce.cart.mapper;

import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.cart.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    // =========================================================================
    // CONVERSIONES BÁSICAS CATEGORIA -> DTO
    // =========================================================================
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(expression = "java(cart.getTotalMount())", target = "totalAmount")
    @Mapping(expression = "java(cart.getTotalItems())", target = "totalItems")
    @Mapping(expression = "java(cart.getTotalUniqueItems())", target = "totalUniqueItems")
    @Mapping(source = "items", target = "items")
    CartDTO toCartDTO(Cart cart);

    // =========================================================================
    // CONVERSIONES BÁSICAS DTO -> ENTIDAD
    // =========================================================================
    /**
     * Convierte un CartDTO a una entidad Cart.
     * Esta función es útil para procesos que necesitan trabajar con la entidad.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "id", ignore = true)
    Cart toCart(CartDTO cartDTO);

}

