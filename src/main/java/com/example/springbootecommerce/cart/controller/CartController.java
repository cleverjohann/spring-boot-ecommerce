package com.example.springbootecommerce.cart.controller;

import com.example.springbootecommerce.cart.dto.AddItemDTO;
import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.cart.dto.UpdateItemDTO;
import com.example.springbootecommerce.cart.service.CartService;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import com.example.springbootecommerce.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.springbootecommerce.shared.util.Constants.CART_ENDPOINT;

/**
 * Controlador REST para la gestión del carrito de compras.
 * Este controlador permite a los usuarios autenticados ver su carrito, agregar, actualizar y eliminar artículos, y validar el stock.
 */
@Slf4j
@RestController
@RequestMapping(CART_ENDPOINT)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Carrito", description = "API para gestion de carritos")
public class CartController {

    private final CartService cartService;

    // ========================================================================
    // OPERACIONES DE CONSULTA
    // ========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal User currentUser) {
        log.debug("Obteniendo carrito de usuario con ID: {}", currentUser.getId());

        CartDTO cart = cartService.getCartWithStockValidation(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cart, "Carrito obtenido exitosamente"));
    }

    @GetMapping("/validate-stock")
    public ResponseEntity<ApiResponse<Boolean>> validateCartStock(@AuthenticationPrincipal User currentUser) {
        log.debug("Validando stock del carrito para usuario: {}", currentUser.getId());
        boolean isValid = cartService.validateCartStock(currentUser.getId());
        String message = isValid ? "Stock disponible para todos los productos"
                : "No hay stock disponible para algunos productos";
        return ResponseEntity.ok(ApiResponse.success(isValid, message));
    }


    // ========================================================================
    // OPERACIONES DE MODIFICACIÓN
    // ========================================================================

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddItemDTO addItemDTO
    ) {
        log.debug("Agregando item al carrito -Usuario: {}, Producto:{}, Cantidad:{}",
                currentUser.getId(), addItemDTO.getProductId(), addItemDTO.getQuantity());

        CartDTO updateCart = cartService.addItem(currentUser.getId(), addItemDTO);
        return ResponseEntity.ok(ApiResponse.success(updateCart, "Item agregado exitosamente al carrito"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItemQuantity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemDTO updateItemDTO
    ) {
        log.info("Actualizando cantidad de item - Usuario: {}, Item: {} Nueva cantidad: {}",
                currentUser.getEmail(), itemId, updateItemDTO.getQuantity());

        CartDTO updateCart = cartService.updateItemQuantity(currentUser.getId(), itemId, updateItemDTO.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(updateCart, "Item actualizado exitosamente al carrito"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long itemId
    ) {
        log.info("Eliminando item del carrito - Usuario: {}, Item: {}", currentUser.getId(), itemId);

        cartService.removeItem(currentUser.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item eliminado exitosamente del carrito"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(@AuthenticationPrincipal User currentUser) {
        log.info("Limpiando carrito - Usuario: {}", currentUser.getId());

        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Carrito limpiado exitosamente"));
    }
}
