package com.example.springbootecommerce.cart.mapper;

import com.example.springbootecommerce.cart.dto.CartItemDTO;
import com.example.springbootecommerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.name", target = "productoNombre")
    @Mapping(source = "producto.sku", target = "productoSku")
    @Mapping(expression = "java(cartItem.getProducto() != null ? cartItem.getProducto().getPrice() : java.math.BigDecimal.ZERO)", target = "unitPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(expression = "java(cartItem.getSubtotal())", target = "subtotal")
    @Mapping(source = "producto.imageUrl", target = "productoImageUrl")
    @Mapping(expression = "java(cartItem.getProducto().hasStock(cartItem.getQuantity()) ? \"SI\" : \"NO\")", target = "isAvailable")
    @Mapping(source = "producto.stockQuantity", target = "availableStock")
    @Mapping(source = "addedAt", target = "addedAt")
    CartItemDTO toItemDTO(CartItem cartItem);
}