package com.example.springbootecommerce.cart.service.impl;

import com.example.springbootecommerce.cart.dto.AddItemDTO;
import com.example.springbootecommerce.cart.dto.CartDTO;
import com.example.springbootecommerce.cart.dto.CartItemDTO;
import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import com.example.springbootecommerce.cart.mapper.CartMapper;
import com.example.springbootecommerce.cart.repository.CartItemRepository;
import com.example.springbootecommerce.cart.repository.CartRepository;
import com.example.springbootecommerce.cart.service.CartService;
import com.example.springbootecommerce.product.entity.Producto;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.user.entity.User;
import com.example.springbootecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductoRepository productoRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    // ========================================================================
    // OPERACIONES DE CONSULTA
    // ========================================================================

    @Override
    public CartDTO getCartForUser(Long userId) {
        log.debug("Obteniendo carrito de usuario con ID: {}", userId);

        Cart cart =findOrCreateCart(userId);
        return cartMapper.toCartDTO(cart);
    }

    @Override
    public CartDTO getCartForUser(User user) {
        log.debug("Obteniendo carrito de usuario con ID: {}", user.getId());
        return getCartForUser(user.getId());
    }

    // ========================================================================
    // OPERACIONES DE MODIFICACIÓN
    // ========================================================================

    @Override
    @Transactional
    public CartDTO addItem(Long userId, AddItemDTO addItemDTO) {
        log.info("Agregando item al carrito - Usuario: {}, Producto:{}, Cantidad:{}", userId, addItemDTO.getProductId(), addItemDTO.getQuantity());

        Producto producto = productoRepository.findById(addItemDTO.getProductId())
                .orElseThrow(()-> new ResourceNotFoundException("Producto no encontrado con ID: "));

        if (!Boolean.TRUE.equals(producto.getIsActive())){
            throw new BusinessException("El producto no esta disponible");
        }
        if (!producto.hasStock(addItemDTO.getQuantity())){
            throw new BusinessException("Stock insuficiente. Disponible: " + producto.getStockQuantity());
        }

        Cart cart = findOrCreateCart(userId);

        CartItem existingItem = cart.findItemByProductId(addItemDTO.getProductId());
        if (existingItem != null){
            int newQuantity = existingItem.getQuantity() + addItemDTO.getQuantity();
            if (!producto.hasStock(newQuantity)){
                throw new BusinessException("Stock insuficiente. Disponible: " + producto.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        }else {
            CartItem newItem = new CartItem(cart, producto, addItemDTO.getQuantity());
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart = cartRepository.save(cart);
        log.info("Item agregado exitosamente al carrito");
        return cartMapper.toCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        log.info("Actualizando cantidad de item - Usuario: {}, Item: {}, Nueva cantidad: {}", userId, itemId, quantity);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(()-> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getCart().getUser().getId().equals(userId)){
            throw new BusinessException("El item no pertenece al usuario con ID: " + userId);
        }

        Producto producto = item.getProducto();
        if (!producto.hasStock(quantity)){
            throw new BusinessException("Stock insuficiente. Disponible: " + producto.getStockQuantity());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart cart = item.getCart();
        log.info("Cantidad actualizada exitosamente del carrito");
        return cartMapper.toCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO removeItem(Long userId, Long itemId) {
        log.info("Eliminando item del carrito - Usuario: {}, Item: {}", userId, itemId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(()-> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getCart().getUser().getId().equals(userId)){
            throw new BusinessException("El item no pertenece al usuario con ID: " + userId);
        }

        Cart cart = item.getCart();
        cart.removeItem(item);
        cartItemRepository.delete(item);

        log.info("Item eliminado exitosamente del carrito");
        return cartMapper.toCartDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        log.info("Limpiando carrito - Usuario: {}", userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Carrito no encontrado"));

        cart.clear();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.save(cart);

        log.info("Carrito limpiado exitosamente");
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        clearCart(user.getId());
    }

    @Override
    public boolean validateCartStock(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        if (cart == null || cart.isEmpty()){
            return true;
        }
        return cart.validateStock();
    }

    @Override
    public CartDTO getCartWithStockValidation(Long userId) {
        CartDTO cartDTO = getCartForUser(userId);
        return null;
    }

    // ========================================================================
    // MÉTODOS PRIVADOS
    // ========================================================================
    private Cart findOrCreateCart(Long userId){
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(()-> createCartForUser(userId));
    }

    private Cart createCartForUser(Long userId){
        log.debug("Creando carrito para usuario con ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        Cart cart = new Cart(user);
        return cartRepository.save(cart);
    }
}
