package com.example.springbootecommerce.order.repository.specification;

import com.example.springbootecommerce.order.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> userId(Long userid){
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userid);
    }

    public static Specification<Order> status(Order.OrderStatus status){
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> guestEmail(String email){
        return (root, query, cb) -> cb.equal(root.get("guestEmail"), email);
    }

    public static Specification<Order> orderDateBetween(LocalDateTime start, LocalDateTime end){
        return (root, query, cb) -> cb.between(root.get("orderDate"), start, end);
    }
    // ========================================================================
    // NUEVAS ESPECIFICACIONES PARA BÚSQUEDA AVANZADA
    // ========================================================================

    /**
     * Busca por email de cliente (usuarios registrados o invitados)
     */
    public static Specification<Order> customerEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) {
                return cb.conjunction(); // No filtrar si está vacío
            }

            String pattern = "%" + email.toLowerCase() + "%";
            var userEmailPredicate = cb.like(cb.lower(root.join("user").get("email")), pattern);
            var guestEmailPredicate = cb.like(cb.lower(root.get("guestEmail")), pattern);

            return cb.or(userEmailPredicate, guestEmailPredicate);
        };
    }

    /**
     * Busca por nombre de cliente (usuarios registrados o invitados)
     */
    public static Specification<Order> customerName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction(); // No filtrar si está vacío
            }

            String pattern = "%" + name.toLowerCase() + "%";

            // Para usuarios registrados
            var userFirstName = cb.like(cb.lower(root.join("user").get("firstName")), pattern);
            var userLastName = cb.like(cb.lower(root.join("user").get("lastName")), pattern);
            var userFullName = cb.like(
                    cb.lower(cb.concat(cb.concat(root.join("user").get("firstName"), " "),
                            root.join("user").get("lastName"))), pattern);

            // Para invitados
            var guestFirstName = cb.like(cb.lower(root.get("guestFirstName")), pattern);
            var guestLastName = cb.like(cb.lower(root.get("guestLastName")), pattern);
            var guestFullName = cb.like(
                    cb.lower(cb.concat(cb.concat(root.get("guestFirstName"), " "),
                            root.get("guestLastName"))), pattern);

            return cb.or(
                    cb.or(userFirstName, userLastName, userFullName),
                    cb.or(guestFirstName, guestLastName, guestFullName)
            );
        };
    }

    /**
     * Filtra por fecha de inicio (mayor o igual)
     */
    public static Specification<Order> orderDateAfter(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("orderDate"), startDate);
        };
    }

    /**
     * Filtra por fecha de fin (menor o igual)
     */
    public static Specification<Order> orderDateBefore(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("orderDate"), endDate);
        };
    }

    // ========================================================================
    // FUNCIÓN DE UTILIDAD PARA COMBINAR MÚLTIPLES ESPECIFICACIONES
    // ========================================================================

    /**
     * Combina múltiples especificaciones con AND
     */
    @SafeVarargs
    public static Specification<Order> combineWithAnd(Specification<Order>... specifications) {
        Specification<Order> result = null;
        for (Specification<Order> spec : specifications) {
            if (spec != null) {
                result = (result == null) ? spec : result.and(spec);
            }
        }
        return result;
    }

}
