package com.example.springbootecommerce.order.controller;

import com.example.springbootecommerce.order.dto.*;
import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.order.repository.specification.OrderSpecification;
import com.example.springbootecommerce.order.service.OrderService;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import com.example.springbootecommerce.shared.dto.PageResponse;
import com.example.springbootecommerce.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.springbootecommerce.shared.util.Constants.ORDERS_ENDPOINT;

/**
 * Controlador REST para la gestión completa de órdenes.
 * Maneja endpoints para usuarios autenticados, invitados y administradores, permitiendo la creación, consulta y gestión de órdenes.
 */
@Slf4j
@RestController
@RequestMapping(ORDERS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Orden", description = "API para gestion de ordenes")
public class OrderController {

    private final OrderService orderService;

    // ========================================================================
    // ENDPOINTS PARA USUARIOS AUTENTICADOS
    // ========================================================================

    /**
     * Crear una nueva orden para usuario autenticado
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDTO>> placeOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateOrderDTO createOrderDTO) {

        log.info("Procesando nueva orden para usuario: {}", currentUser.getEmail());

        OrderDTO order = orderService.placeOrder(currentUser, createOrderDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Orden creada exitosamente"));
    }

    /**
     * Obtener todas las órdenes del usuario autenticado
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> getUserOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Obteniendo órdenes para usuario: {}", currentUser.getEmail());

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryDTO> orders = orderService.getUserOrders(currentUser, pageable);
        PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Órdenes obtenidas exitosamente")
        );
    }

    /**
     * Obtener una orden específica del usuario autenticado
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.debug("Obteniendo orden ID: {} para usuario: {}", orderId, currentUser.getEmail());

        OrderDTO order = orderService.getOrderById(orderId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success(order, "Orden obtenida exitosamente")
        );
    }

    /**
     * Obtener órdenes del usuario filtradas por estado
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> getUserOrdersByStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Obteniendo órdenes de usuario {} con estado: {}", currentUser.getEmail(), status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<OrderSummaryDTO> orders = orderService.getUserOrdersByStatus(currentUser, orderStatus, pageable);
            PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

            return ResponseEntity.ok(
                    ApiResponse.success(pageResponse, "Órdenes filtradas por estado obtenidas exitosamente")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Estado de orden inválido: " + status));
        }
    }

    /**
     * Obtener historial de estados de una orden
     */
    @GetMapping("/{orderId}/status-history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryDTO>>> getOrderStatusHistory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long orderId) {

        log.debug("Obteniendo historial de orden ID: {} para usuario: {}", orderId, currentUser.getEmail());

        // Verificar que el usuario puede acceder a la orden
        if (!orderService.canUserAccessOrder(orderId, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tiene permisos para acceder a esta orden"));
        }

        List<OrderStatusHistoryDTO> history = orderService.getOrderStatusHistory(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(history, "Historial de orden obtenido exitosamente")
        );
    }

    // ========================================================================
    // ENDPOINTS PARA INVITADOS
    // ========================================================================

    /**
     * Crear una nueva orden para usuario invitado
     */
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<OrderDTO>> placeGuestOrder(
            @Valid @RequestBody CreateGuestOrderDTO createGuestOrderDTO) {

        log.info("Procesando orden de invitado: {}", createGuestOrderDTO.getGuestEmail());

        OrderDTO order = orderService.placeGuestOrder(createGuestOrderDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Orden de invitado creada exitosamente"));
    }

    /**
     * Consultar órdenes de invitado por email y rango de fechas
     */
    @GetMapping("/guest/search")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> getGuestOrders(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Consultando órdenes de invitado: {} entre {} y {}", email, startDate, endDate);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryDTO> orders = orderService.getGuestOrdersByEmailAndDateRange(
                email, startDate, endDate, pageable);
        PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Órdenes de invitado obtenidas exitosamente")
        );
    }

    /**
     * Búsqueda de órdenes de invitado por email (usando especificaciones)
     */
    @GetMapping("/admin/search/guest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> searchGuestOrders(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Buscando órdenes de invitado: {}", email);

        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Order> spec = OrderSpecification.combineWithAnd(
                OrderSpecification.guestEmail(email),
                startDate != null && endDate != null ?
                        OrderSpecification.orderDateBetween(startDate, endDate) : null
        );

        Page<OrderSummaryDTO> orders = orderService.searchOrders(spec, pageable);
        PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Órdenes de invitado encontradas exitosamente")
        );
    }

    // ========================================================================
    // ENDPOINTS ADMINISTRATIVOS
    // ========================================================================

    /**
     * Obtener todas las órdenes del sistema (solo administradores)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Obteniendo todas las órdenes - Admin");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryDTO> orders = orderService.getAllOrders(pageable);
        PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Todas las órdenes obtenidas exitosamente")
        );
    }

    /**
     * Obtener una orden específica por ID (solo administradores)
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByIdAdmin(@PathVariable Long orderId) {

        log.debug("Obteniendo orden ID: {} - Admin", orderId);

        OrderDTO order = orderService.getOrderById(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(order, "Orden obtenida exitosamente")
        );
    }

    /**
     * Actualizar el estado de una orden (solo administradores)
     */
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusDTO updateStatusDTO) {

        log.info("Actualizando estado de orden ID: {} a: {}", orderId, updateStatusDTO.getStatus());

        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, updateStatusDTO);

        return ResponseEntity.ok(
                ApiResponse.success(updatedOrder, "Estado de orden actualizado exitosamente")
        );
    }

    /**
     * Búsqueda específica por usuario y estado (usando especificaciones)
     */
    @GetMapping("/admin/search/user-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> searchOrdersByUserAndStatus(
            @RequestParam Long userId,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Buscando órdenes de usuario ID: {} con estado: {}", userId, status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Usar especificaciones directamente
            Specification<Order> spec = OrderSpecification.combineWithAnd(
                    OrderSpecification.userId(userId),
                    OrderSpecification.status(orderStatus)
            );

            Page<OrderSummaryDTO> orders = orderService.searchOrders(spec, pageable);
            PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

            return ResponseEntity.ok(
                    ApiResponse.success(pageResponse, "Órdenes encontradas exitosamente")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Estado de orden inválido: " + status));
        }
    }


    /**
     * Cancelar una orden específica (solo administradores)
     */
    @PutMapping("/admin/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderDTO cancelOrderDTO) {

        log.info("Cancelando orden ID: {}", orderId);

        String reason = (cancelOrderDTO != null && cancelOrderDTO.getReason() != null)
                ? cancelOrderDTO.getReason()
                : "Cancelación administrativa";

        OrderDTO cancelledOrder = orderService.cancelOrder(orderId, reason);

        return ResponseEntity.ok(
                ApiResponse.success(cancelledOrder, "Orden cancelada exitosamente")
        );
    }

    /**
     * Marcar múltiples órdenes como enviadas (solo administradores)
     */
    @PutMapping("/admin/bulk/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> markOrdersAsShipped(
            @Valid @RequestBody BulkShipOrdersDTO bulkShipDTO) {

        log.info("Marcando {} órdenes como enviadas", bulkShipDTO.getOrderIds().size());

        List<OrderDTO> shippedOrders = orderService.markOrdersAsShipped(bulkShipDTO.getOrderIds());

        return ResponseEntity.ok(
                ApiResponse.success(shippedOrders,
                        shippedOrders.size() + " órdenes marcadas como enviadas exitosamente")
        );
    }

    // ========================================================================
    // ENDPOINTS DE REPORTES Y ESTADÍSTICAS (ADMINISTRADORES)
    // ========================================================================

    /**
     * Obtener reporte de ingresos por rango de fechas
     */
    @GetMapping("/admin/reports/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueReportDTO>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Generando reporte de ingresos entre {} y {}", startDate, endDate);

        RevenueReportDTO report = orderService.getRevenuesReport(startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.success(report, "Reporte de ingresos generado exitosamente")
        );
    }

    /**
     * Obtener estadísticas generales de órdenes
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderStatsDTO>> getOrdersStatistics() {

        log.info("Obteniendo estadísticas generales de órdenes");

        OrderStatsDTO stats = orderService.getOrdersStatistics();

        return ResponseEntity.ok(
                ApiResponse.success(stats, "Estadísticas obtenidas exitosamente")
        );
    }

    /**
     * Obtener órdenes que requieren atención
     */
    @GetMapping("/admin/requiring-action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ActionRequiredOrdersDTO>> getOrdersRequiringAction() {

        log.info("Obteniendo órdenes que requieren atención");

        ActionRequiredOrdersDTO actionRequired = orderService.getOrdersRequiringAction();

        return ResponseEntity.ok(
                ApiResponse.success(actionRequired, "Órdenes que requieren atención obtenidas exitosamente")
        );
    }

    // ========================================================================
    // ENDPOINTS DE BÚSQUEDA AVANZADA (ADMINISTRADORES)
    // ========================================================================

    /**
     * Búsqueda avanzada de órdenes con filtros múltiples
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryDTO>>> searchOrders(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String customerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Realizando búsqueda avanzada de órdenes - Email: {}, Estado: {}, Fechas: {} - {}, Cliente: {}",
                customerEmail, status, startDate, endDate, customerName);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Validar estado si se proporciona
        Order.OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Estado de orden inválido: " + status));
            }
        }

        // Construir especificaciones usando OrderSpecification
        Specification<Order> spec = buildOrderSpecification(
                customerEmail, orderStatus, startDate, endDate, customerName);

        Page<OrderSummaryDTO> orders = orderService.searchOrders(spec, pageable);
        PageResponse<OrderSummaryDTO> pageResponse = PageResponse.of(orders);

        return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Búsqueda de órdenes completada exitosamente")
        );
    }

    // ========================================================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================================================

    /**
     * Construye la especificación de búsqueda usando OrderSpecification
     */
    private Specification<Order> buildOrderSpecification(
            String customerEmail,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String customerName) {

        return OrderSpecification.combineWithAnd(
                OrderSpecification.customerEmail(customerEmail),
                status != null ? OrderSpecification.status(status) : null,
                OrderSpecification.orderDateAfter(startDate),
                OrderSpecification.orderDateBefore(endDate),
                OrderSpecification.customerName(customerName)
        );
    }

}
