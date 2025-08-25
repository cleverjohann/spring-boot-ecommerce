package com.example.springbootecommerce.cart.repository;

import com.example.springbootecommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductoId(Long cartId, Long productId);

    void deleteByCartIdAndProductoId(Long cartId, Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.producto.id = :productId")
    Optional<CartItem> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);


    void deleteByCartId(Long cartId);

}
