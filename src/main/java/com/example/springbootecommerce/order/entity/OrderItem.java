package com.example.springbootecommerce.order.entity;

import com.example.springbootecommerce.product.entity.Producto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"order"})
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Producto producto;

    @Column(name = "product_name", nullable = false)
    private String productoName;

    @Column(name = "product_sku", nullable = false)
    private String productoSku;

    @Column(name = "price_at_purchase", nullable = false)
    private BigDecimal priceAtPurchase;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public OrderItem() {
    }

    public OrderItem(Producto producto, Integer quantity) {
        this.producto = producto;
        this.productoName = producto.getName();
        this.productoSku = producto.getSku();
        this.priceAtPurchase = producto.getPrice();
        this.quantity = quantity;
        this.subtotal = calculateSubtotal();
    }

    public BigDecimal calculateSubtotal(){
        if (priceAtPurchase != null && quantity != null){
            return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    private void updateSubtotal(){
        this.subtotal = calculateSubtotal();
    }
}
