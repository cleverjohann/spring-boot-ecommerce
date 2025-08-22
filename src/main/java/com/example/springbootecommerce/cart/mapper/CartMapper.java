package com.example.springbootecommerce.cart.mapper;

import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.cart.dto.CartItemDTO;
import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    // =========================================================================
    // CONVERSIONES BÁSICAS CATEGORIA -> DTO
    // =========================================================================
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(expression = "java(cart.getTotalMount())", target = "totalAmount")
    @Mapping(source = "items", target = "items")
    CartDTO toCartDTO(Cart cart);

    /**
     * Convierte una lista de entidades Categoria a una lista de CartItemDTO.
     */
    List<CartItemDTO> toItemDTOList(List<CartItem> cartItems);

    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.name", target = "productoNombre")
    @Mapping(source = "producto.sku", target = "productoSku")
    @Mapping(expression = "java(cartItem.getProducto() != null ? cartItem.getProducto().getPrice() : java.math.BigDecimal.ZERO)", target = "unitPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(expression = "java(cartItem.getTotal())", target = "subtotal")
    @Mapping(source = "producto.imageUrl", target = "productoImageUrl")
    @Mapping(expression = "java(cartItem.isAvailable() ? \"SI\" : \"NO\")", target = "isAvailable")
    @Mapping(source = "producto.stockQuantity", target = "availableStock")
    @Mapping(source = "addedAt", target = "addedAt")
    CartItemDTO toItemDTO(CartItem cartItem);
}
