package com.example.springbootecommerce.cart.entity;

import com.example.springbootecommerce.product.entity.Producto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"cart"})
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Producto producto;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public CartItem(){
        this.addedAt = LocalDateTime.now();
    }

    public CartItem(Producto producto, Integer quantity){
        this.producto = producto;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    public CartItem(Cart cart, Producto producto, Integer quantity){
        this.cart = cart;
        this.producto = producto;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    // ========================================================================
    // MÉTODOS DE NEGOCIO
    // ========================================================================

    public BigDecimal getSubtotal(){
        return producto != null ? producto.getPrice() : BigDecimal.ZERO;
    }

    public BigDecimal getTotal(){
        if (producto == null || quantity == null){
            return BigDecimal.ZERO;
        }
        return producto.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isAvailable(){
        return producto != null && producto.hasStock(quantity);
    }

    public boolean isValidQuantity(){
        return quantity != null && quantity > 0;
    }

    @PrePersist
    @PreUpdate
    private void validate(){
        if (quantity == null || quantity <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (addedAt == null){
            addedAt = LocalDateTime.now();
        }
    }
}
