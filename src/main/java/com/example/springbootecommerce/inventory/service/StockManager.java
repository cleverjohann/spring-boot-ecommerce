package com.example.springbootecommerce.inventory.service;

import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.order.entity.OrderItem;
import com.example.springbootecommerce.product.entity.Producto;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockManager {

    private final ProductoRepository productoRepository;

    /**
     * Reserva el stock para todos los items del carrito
     * Usa el bloqueo pesimista para evitar condiciones de carrera en concurrencia alta
     */
    public void reserveStock(Cart cart){
        for (CartItem item : cart.getItems()){
            Producto producto = productoRepository.findByIdWithLock(item.getProducto().getId())
                    .orElseThrow(()-> new ResourceNotFoundException("Producto no encontrado: " +
                            item.getProducto().getId()));
            if (!producto.hasStock(item.getQuantity())){
                throw new BusinessException(
                        String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                                producto.getName(), producto.getStockQuantity(), item.getQuantity())
                );
            }
            producto.reduceStock(item.getQuantity());
            productoRepository.save(producto);
        }
    }

    /**
     * Restaura el stock de una orden cancelada
     */
    public void restoreStock(Order order){
        for (OrderItem item : order.getItems()){
            Producto producto = productoRepository.findById(item.getProducto().getId()).orElse(null);
            if (producto != null){
                producto.increaseStock(item.getQuantity());
                productoRepository.save(producto);
            }else {
                log.error("Producto no encontrado al restaurar el stock de la orden: {}", order.getId());
            }
        }
    }

}
