package com.example.springbootecommerce.cart.entity;

import com.example.springbootecommerce.shared.audit.Auditable;
import com.example.springbootecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@SuperBuilder
@Table(name = "carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // ========================================================================
    // MÉTODOS DE NEGOCIO
    // ========================================================================
    public BigDecimal getTotalMount(){
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems(){
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public int getTotalUniqueItems(){
        return items.size();
    }

    public boolean isEmpty(){
        return items.isEmpty();
    }

    public boolean hasItems(Long productId){
        return items.stream()
                .anyMatch(item -> item.getProducto().getId().equals(productId));
    }

    public CartItem findItemByProductId(Long productId){
        return items.stream()
                .filter(item -> item.getProducto().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public void addItem(CartItem item){
        CartItem existingItem = findItemByProductId(item.getProducto().getId());
        if (existingItem != null){
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        }else{
            items.add(item);
            item.setCart(this);
        }
    }

    public void removeItem(CartItem item){
        items.remove(item);
        item.setCart(null);
    }

    public void removeItemByProductId(Long productId){
        CartItem item = findItemByProductId(productId);
        if (item != null){
            removeItem(item);
        }
    }

    public void clear(){
        items.forEach(item -> item.setCart(null));
        items.clear();
    }

    public void updateItemQuantity(Long productoid, int quantity){
        CartItem item = findItemByProductId(productoid);
        if (item != null){
            if (quantity <= 0){
                removeItem(item);
            }else {
                item.setQuantity(quantity);
            }
        }
    }

    public boolean validateStock(){
        return items.stream()
                .allMatch((item -> item.getProducto().hasStock(item.getQuantity())));
    }

    public List<CartItem> getItemsWithInsufficientStock(){
        return items.stream()
                .filter(item -> !item.getProducto().hasStock(item.getQuantity()))
                .toList();
    }
}
