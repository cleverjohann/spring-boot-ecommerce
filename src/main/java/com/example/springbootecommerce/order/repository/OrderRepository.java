package com.example.springbootecommerce.order.repository;

import com.example.springbootecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // JOIN FETCH: Orden con items y pago para evitar N+1
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.payment WHERE o.id = :orderId")
    Optional<Order> findByIdWithItemsAndPayment(@Param("orderId") Long orderId);

    // JOIN FETCH: Órdenes de usuario con items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    // Estadística: contar por estado
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Order.OrderStatus status);

    // Estadística: total de ingresos por rango de fechas y estado entregado/enviado
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('DELIVERED', 'SHIPPED') AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Lógica de acción: órdenes listas para enviar
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.orderDate < :cutoffDate")
    List<Order> findOrdersReadyToShip(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Lógica de acción: órdenes pendientes de entrega
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND o.shippedDate < :cutoffDate")
    List<Order> findOrdersPendingDelivery(@Param("cutoffDate") LocalDateTime cutoffDate);


    Page<Order> findByUserIdOrderByOrderDateDesc(Long id, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long id, Order.OrderStatus status, Pageable pageable);

    Page<Order> findByGuestEmailAndOrderDateBetween(String guestEmail, LocalDateTime orderDateAfter, LocalDateTime orderDateBefore, Pageable pageable);

    List<Order> findByStatusAndOrderDateBetween(Order.OrderStatus orderStatus, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusAndOrderDateBefore(Order.OrderStatus status, LocalDateTime orderDateBefore);

    boolean existsByIdAndGuestEmail(Long id, String guestEmail);

    boolean existsByIdAndUserId(Long id, Long userId);
}
