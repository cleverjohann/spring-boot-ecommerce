package com.example.springbootecommerce.cart.repository;

import com.example.springbootecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserEmail(String email);
    /**
     * Busca el carrito de un usuario por su ID, trayendo también todos los items del carrito
     * y los productos asociados a cada item en una sola consulta (fetch join).
     * Esto evita múltiples consultas adicionales al acceder a los items y productos del carrito.
     *
     * @param userId ID del usuario dueño del carrito
     * @return Optional con el carrito y sus items/productos cargados, o vacío si no existe
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.producto WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserEmail(String email);

    void deleteByUserId(Long userId);
}
