package com.example.springbootecommerce.cart.mapper;

import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.cart.dto.CartItemDTO;
import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import com.example.springbootecommerce.product.entity.Producto;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

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

    // =========================================================================
    // CONVERSIONES BÁSICAS DTO -> ENTIDAD
    // =========================================================================
    /**
     * Convierte un CartDTO a una entidad Cart.
     * Esta función es útil para procesos que necesitan trabajar con la entidad.
     */
    @Mapping(target = "user", ignore = true) // Se asigna manualmente
    @Mapping(target = "id", source = "id")
    @Mapping(target = "items", source = "items", qualifiedByName = "itemDTOListToEntityList")
    @Mapping(target = "updatedAt", source = "updatedAt")
    public abstract Cart toEntity(CartDTO cartDTO);

}
