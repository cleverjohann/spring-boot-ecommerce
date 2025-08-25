package com.example.springbootecommerce.cart.service;

import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import com.example.springbootecommerce.cart.repository.CartRepository;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartManager {

    private final CartRepository cartRepository;

    /**
     * Obtiene la ENTIDAD Cart para un usuario.
     * Diseñado para ser usado por otros servicios (capa interna).
     * Lanza excepciones si el carrito no es válido para un checkout.
     */
    public Cart getActiveUserCart(User user) {
        Cart cart = cartRepository.findByUserEmail(user.getEmail())
                .orElseThrow(()-> new BusinessException("El usuario no tiene un carrito activo"));

        if (cart.isEmpty()){
            throw new BusinessException("No se puede procesar una orden con un carrito vacío");
        }

        if (!cart.validateStock()) {
            List<CartItem> invalidItems = cart.getItemsWithInsufficientStock();
            String invalidProductos = invalidItems.stream()
                    .map(item -> item.getProducto().getName())
                    .collect(Collectors.joining(", "));
            throw new BusinessException("No se puede procesar una orden con productos sin stock: " + invalidProductos);
        }
        return cart;
    }

    /**
     * Vacía el carrito de un usuario después de una compra exitosa
     */
    public void clearUserCart(User user){
        Cart cart = getActiveUserCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
