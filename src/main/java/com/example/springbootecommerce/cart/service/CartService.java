package com.example.springbootecommerce.cart.service;

import com.example.springbootecommerce.cart.dto.AddItemDTO;
import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.user.entity.User;

/**
 * Servicio para la gestión del carrito de compras.
 * Define las operaciones principales para consultar y modificar el carrito.
 */
public interface CartService {

    // ========================================================================
    // OPERACIONES DE CONSULTA
    // ========================================================================
    /**
     * Obtiene el carrito de un usuario por su ID.
     * @param userId ID del usuario
     * @return Carrito en formato DTO
     */
    CartDTO getCartForUser(Long userId);

    /**
     * Obtiene el carrito de un usuario a partir de la entidad User.
     * @param user Entidad usuario
     * @return Carrito en formato DTO
     */
    CartDTO getCartForUser(User user);

    // ========================================================================
    // OPERACIONES DE MODIFICACIÓN
    // ========================================================================

    /**
     * Agrega un ítem al carrito del usuario.
     * @param userId ID del usuario
     * @param addItemDTO Datos del producto y cantidad a agregar
     * @return Carrito actualizado en formato DTO
     */
    CartDTO addItem(Long userId, AddItemDTO addItemDTO);

    /**
     * Actualiza la cantidad de un ítem en el carrito.
     * @param userId ID del usuario
     * @param itemId ID del ítem en el carrito
     * @param quantity Nueva cantidad
     * @return Carrito actualizado en formato DTO
     */
    CartDTO updateItemQuantity(Long userId, Long itemId, Integer quantity);

    /**
     * Elimina un ítem del carrito.
     * @param userId ID del usuario
     * @param itemId ID del ítem a eliminar
     * @return Carrito actualizado en formato DTO
     */
    CartDTO removeItem(Long userId, Long itemId);

    /**
     * Vacía el carrito del usuario.
     * @param userId ID del usuario
     */
    void clearCart(Long userId);

    /**
     * Vacía el carrito del usuario a partir de la entidad User.
     * @param user Entidad usuario
     */
    void clearCart(User user);

    // ========================================================================
    // MÉTODOS DE VALIDACIÓN
    // ========================================================================

    /**
     * Valida si hay stock suficiente para todos los productos del carrito.
     * @param userId ID del usuario
     * @return true si hay stock suficiente, false en caso contrario
     */
    boolean validateCartStock(Long userId);

    /**
     * Obtiene el carrito validando el stock de los productos.
     * @param userId ID del usuario
     * @return Carrito en formato DTO con validación de stock
     */
    CartDTO getCartWithStockValidation(Long userId);

}
