package com.example.springbootecommerce.order.service.impl;

import com.example.springbootecommerce.cart.entity.Cart;
import com.example.springbootecommerce.cart.entity.CartItem;
import com.example.springbootecommerce.cart.mapper.CartMapper;
import com.example.springbootecommerce.cart.service.CartManager;
import com.example.springbootecommerce.cart.service.CartService;
import com.example.springbootecommerce.inventory.service.StockManager;
import com.example.springbootecommerce.notification.service.EmailService;
import com.example.springbootecommerce.order.dto.*;
import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.order.entity.OrderItem;
import com.example.springbootecommerce.order.mapper.OrderMapper;
import com.example.springbootecommerce.order.repository.OrderRepository;
import com.example.springbootecommerce.order.service.OrderService;
import com.example.springbootecommerce.payment.service.PaymentService;
import com.example.springbootecommerce.product.entity.Producto;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import com.example.springbootecommerce.product.service.ProductoService;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.user.entity.Address;
import com.example.springbootecommerce.user.entity.User;
import com.example.springbootecommerce.user.repository.AddressRepository;
import com.example.springbootecommerce.user.service.AddressValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final ProductoService productoService;
    private final ProductoRepository productoRepository;

    private final CartManager cartManager;
    private final StockManager stockManager;
    private final AddressValidator addressValidator;

    // ========================================================================
    // PROCESAMIENTO DE ÓRDENES - MÉTODOS TRANSACCIONALES CRÍTICOS
    // ========================================================================

    /**
     * Funcionalidad transaccional crítico para procesar una orden de usuario registrado
     * Este es el corazón del sistema de e-commerce
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO placeOrder(User user, CreateOrderDTO createOrderDTO) {
        log.info("Procesando orden para el usuario {}", user.getEmail());

        try {
            // 1. Validar y obtener el carrito del usuario
            Cart cart = cartManager.getActiveUserCart(user);

            // 2. Validar y obtener dirección de envío
            Address shippingAddress = addressValidator.validateUserAddress(
                    user.getId(),
                    createOrderDTO.getShippingAddressId());

            // 3. Validar stock y reservar productos (con bloqueo pesimista)
            stockManager.reserveStock(cart);

            // 4. Crear la orden con snapshot de datos
            Order order = createOrderEntity(user, cart, shippingAddress, createOrderDTO);

            // 5. Procesar el pago
            paymentService.processPayment(order, createOrderDTO.getPaymentMethod());

            // 6. Confirmar la orden y reducir stock definitivamente
            order.markAsConfirmed();
            Order savedOrder = orderRepository.save(order);

            // 7. Vaciar el carrito del usuario
            cartManager.clearUserCart(user);

            // 8. Enviar notificación asíncrona
            sendOrderConfirmation(savedOrder);

            log.info("Orden procesada correctamente. ID: {}", savedOrder.getId());
            return orderMapper.toOrderDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error procesando orden para: {} : {}", user.getEmail(), e.getMessage());
            throw new BusinessException("Error procesando orden" + e.getMessage());
        }
    }

    /**
     * Funcionalidad transaccional para órdenes de invitados
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO placeGuestOrder(CreateGuestOrderDTO createGuestOrderDTO) {
        log.info("Iniciando proceso de orden para invitado : {}", createGuestOrderDTO.getGuestEmail());

        try {
            // 1. Validar items del carrito invitado
            List<CartItem> guestCartItems = validateGuestCartItems(createGuestOrderDTO.getCartItems());

            // 2. Validar y reservar stock
            validateAndReserveStockForGuestItems(guestCartItems);

            // 3. Crear orden para invitado
            Order order = createGuestOrder(createGuestOrderDTO, guestCartItems);

            // 4. Procesar pago
            paymentService.processPayment(order, createGuestOrderDTO.getPaymentMethod());

            // 5. Confirmar orden
            order.markAsConfirmed();
            Order savedOrder = orderRepository.save(order);

            // 6. Enviar notificación
            sendOrderConfirmation(savedOrder);

            log.info("Orden procesada correctamente para invitado. ID: {}", savedOrder.getId());
            return orderMapper.toOrderDTO(savedOrder);
        } catch (Exception e) {
            log.error("Error procesando orden para invitado: {} : {}",
                    createGuestOrderDTO.getGuestEmail(), e.getMessage());
            throw new BusinessException("Error procesando orden" + e.getMessage());
        }
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItemsAndPayment(orderId)
                .orElseThrow(() -> new BusinessException("Orden no encontrada"));
        return orderMapper.toOrderDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long orderId, User user) {
        Order order = orderRepository.findByIdWithItemsAndPayment(orderId)
                .orElseThrow(() -> new BusinessException("Orden no encontrada"));

        // Validar permisos pertenece al usuario (si no es admin)
        if (!user.isAdmin() && !order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("No tiene permisos para acceder a esta orden");
        }

        return orderMapper.toOrderDTO(order);
    }

    @Override
    public Page<OrderSummaryDTO> getUserOrders(User user, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId(), pageable);
        return orders.map(orderMapper::toSummaryDTO);
    }

    @Override
    public Page<OrderSummaryDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toSummaryDTO);
    }

    // ========================================================================
    // GESTIÓN ADMINISTRATIVA
    // ========================================================================

    @Override
    public Page<OrderSummaryDTO> searchOrders(Specification<Order> spec, Pageable pageable) {
        log.debug("Buscando ordenes con Specification: {}", spec);

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(orderMapper::toSummaryDTO);
    }

    @Override
    public Page<OrderSummaryDTO> getUserOrdersByStatus(User user, Order.OrderStatus status, Pageable pageable) {
        log.debug("Buscando ordenes de usuario : {} con estado: {}", user.getEmail(), status);
        Page<Order> orders = orderRepository.findByUserIdAndStatusOrderByOrderDateDesc(
                user.getId(), status, pageable);
        return orders.map(orderMapper::toSummaryDTO);
    }

    @Override
    public Page<OrderSummaryDTO> getGuestOrdersByEmailAndDateRange(String guestEmail, LocalDateTime startDate,
                                                                   LocalDateTime endDate, Pageable pageable) {
        log.debug("Obteniendo órdenes de invitado {} entre fechas: {} y {}", guestEmail, startDate, endDate);
        Page<Order> orders = orderRepository.findByGuestEmailAndOrderDateBetween(
                guestEmail, startDate, endDate, pageable
        );
        return orders.map(orderMapper::toSummaryDTO);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusDTO updateStatusDTO) {
        log.info("Actualizando estado de orden con ID: {} a estado: {}", orderId, updateStatusDTO.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Orden no encontrada"));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(updateStatusDTO.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de orden invalido: " + updateStatusDTO.getStatus());
        }

        // Validar transiciones de estado válidas
        validateStatusTransition(order.getStatus(), newStatus);

        // Actualizar estado
        switch (newStatus) {
            case SHIPPED:
                if (!order.canBeShipped()) {
                    throw new BusinessException("No se puede marcar como entregado la orden con ID: " + orderId);
                }
                order.markAsShipped();
                break;
            case DELIVERED:
                if (!order.canBeDelivered()) {
                    throw new BusinessException("No se puede marcar como entregado la orden con ID: " + orderId);
                }
                order.markAsDelivered();
                break;
            case CANCELLED:
                if (!order.canBeCancelled()) {
                    throw new BusinessException("No se puede marcar como cancelada la orden con ID: " + orderId);
                }
                // Restaurar el stock de la orden
                restoreStockForCancelledOrder(order);
                order.markAsCancelled();
                break;
            default:
                order.setStatus(newStatus);
        }
        if (updateStatusDTO.getNotes() != null) {
            order.setNotes(order.getNotes() + "\n[" + LocalDateTime.now() + "] " + updateStatusDTO.getNotes());
        }

        Order savedOrder = orderRepository.save(order);

        // Enviar notificación de cambio de estado
        emailService.sendOrderStatusUpdate(savedOrder);

        log.info("Estado de orden actualizado correctamente. ID: {}", savedOrder.getId());
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO cancelOrder(Long orderId, String reason) {
        log.info("Cancelando orden con ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Orden no encontrada" + orderId));

        if (!order.canBeCancelled()) {
            throw new BusinessException("No se puede cancelar la orden con ID: " + orderId +
                    ". Estado: " + order.getStatus());
        }

        // Restaurar el stock de la orden
        restoreStockForCancelledOrder(order);

        // Marcar la orden como cancelada
        order.markAsCancelled();

        // Agregar notas sobre la cancelación
        String cancelNote = String.format("[%s] Orden cancelada. Motivo: %s",
                LocalDateTime.now(), reason);
        order.setNotes(order.getNotes() != null ?
                order.getNotes() + "\n" + cancelNote : cancelNote);

        Order savedOrder = orderRepository.save(order);

        // Enviar notificación de cancelación
        try {
            emailService.sendOrderStatusUpdate(savedOrder);
        } catch (Exception e) {
            log.error("Error al enviar la notificación para la orden ID: {}", orderId);
        }
        log.info("Orden cancelada correctamente. ID: {}", savedOrder.getId());
        return orderMapper.toOrderDTO(savedOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderDTO> markOrdersAsShipped(List<Long> orderIds) {
        log.info("Marcando ordenes con IDs: {} como entregadas", orderIds);

        List<OrderDTO> shippedOrders = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new BusinessException("Orden no encontrada" + orderId));

                if (!order.canBeShipped()) {
                    throw new BusinessException("No se puede marcar como entregado la orden con ID: " + orderId);
                }

                order.markAsShipped();
                Order savedOrder = orderRepository.save(order);
                shippedOrders.add(orderMapper.toOrderDTO(savedOrder));

                // Enviar notificación
                try {
                    emailService.sendOrderStatusUpdate(savedOrder);
                } catch (Exception e) {
                    log.error("Enviando notificación de entrega para la orden ID: {}", orderId);
                }
            } catch (Exception e) {
                log.error("Error al marcar orden con ID: {} como entregada: {}", orderId, e.getMessage());
                errors.add("Error al marcar orden con ID: " + orderId + " como entregada: " + e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            log.warn("Se han producido errores al marcar las ordenes: {}", errors);
            throw new BusinessException("Se han producido errores al marcar las ordenes: " + errors);
        }
        return shippedOrders;
    }

    @Override
    public RevenueReportDTO getRevenuesReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Generando reporte de ventas entre fechas: {} y {}", startDate, endDate);

        //Obtener estadísticas de la base de datos
        List<Order> deliveredOrders = orderRepository.findByStatusAndOrderDateBetween(
                Order.OrderStatus.DELIVERED, startDate, endDate);

        List<Order> shippedOrders = orderRepository.findByStatusAndOrderDateBetween(
                Order.OrderStatus.SHIPPED, startDate, endDate);

        // Calcular totales
        BigDecimal deliveredRevenue = deliveredOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippedRevenue = shippedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = deliveredRevenue.add(shippedRevenue);

        int totalOrders = deliveredOrders.size() + shippedOrders.size();
        BigDecimal averageOrderValue  = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return RevenueReportDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .deliveredRevenue(deliveredRevenue)
                .shippedRevenue(shippedRevenue)
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredOrders.size())
                .shippedOrders(shippedOrders.size())
                .averageOrderValue(averageOrderValue)
                .build();
    }

    @Override
    public OrderStatsDTO getOrdersStatistics() {
        log.debug("Generando estadísticas generales de ordenes");

        // Contar ordenes por estado
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(Order.OrderStatus.CONFIRMED);
        long shippedOrders = orderRepository.countByStatus(Order.OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        // Obtener ingresos del mes actual
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        RevenueReportDTO revenueReport = getRevenuesReport(startOfMonth, endOfMonth);
        return OrderStatsDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippedOrders(shippedOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .monthlyRevenue(revenueReport.getTotalRevenue())
                .monthlyOrderCount(revenueReport.getTotalOrders())
                .averageOrderValue(revenueReport.getAverageOrderValue())
                .build();
    }

    @Override
    public ActionRequiredOrdersDTO getOrdersRequiringAction() {
        log.debug("Obteniendo órdenes que requieren atención");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime oneWeekAgo = now.minusWeeks(1);

        // Órdenes confirmadas que llevan más de 3 días sin enviar
        List<Order> readyToShipOrders = orderRepository.findByStatusAndOrderDateBefore(
                Order.OrderStatus.CONFIRMED, threeDaysAgo);

        List<Order> pendingDeliveryOrders = orderRepository.findByStatusAndOrderDateBefore(
                Order.OrderStatus.SHIPPED, oneWeekAgo);

        // Órdenes pendientes que llevan más de 24 horas sin confirmar
        LocalDateTime oneDayAgo = now.minusDays(1);
        List<Order> pendingConfirmationOrders = orderRepository.findByStatusAndOrderDateBefore(
                Order.OrderStatus.PENDING, oneDayAgo);

        return ActionRequiredOrdersDTO.builder()
                .readyToShipOrders(readyToShipOrders.stream()
                        .map(orderMapper::toSummaryDTO)
                        .toList())
                .pendingDeliveryOrders(pendingDeliveryOrders.stream()
                        .map(orderMapper::toSummaryDTO)
                        .toList())
                .pendingConfirmationOrders(pendingConfirmationOrders.stream()
                        .map(orderMapper::toSummaryDTO)
                        .toList())
                .totalRequiringAction(readyToShipOrders.size() +
                        pendingDeliveryOrders.size() + pendingConfirmationOrders.size())
                .build();
    }

    @Override
    public boolean canUserAccessOrder(Long orderId, User user) {
        log.debug("Comprobando si el usuario {} puede acceder a la orden con ID: {}", user.getEmail(), orderId);

        // Los administradores pueden acceder a todas las órdenes
        if (user.isAdmin()) {
            return true;
        }
        return orderRepository.existsByIdAndUserId(orderId, user.getId()) ||
                orderRepository.existsByIdAndGuestEmail(orderId, user.getEmail());
    }

    @Override
    public List<OrderStatusHistoryDTO> getOrderStatusHistory(Long orderId) {
        log.debug("Obteniendo historial de estados para orden ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        List<OrderStatusHistoryDTO> history = new ArrayList<>();

        // Crear entrada para estado inicial (siempre PENDING)
        history.add(OrderStatusHistoryDTO.builder()
                .status(Order.OrderStatus.PENDING.name())
                .statusDisplayName(Order.OrderStatus.PENDING.getDisplayName())
                .timestamp(order.getOrderDate())
                .notes("Orden creada")
                .build());

        // Si está confirmada, agregar entrada
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            history.add(OrderStatusHistoryDTO.builder()
                    .status(Order.OrderStatus.CONFIRMED.name())
                    .statusDisplayName(Order.OrderStatus.CONFIRMED.getDisplayName())
                    .timestamp(order.getOrderDate().plusMinutes(1)) // Estimación
                    .notes("Orden confirmada y pago procesado")
                    .build());
        }

        // Si está enviada, agregar entrada
        if (order.getShippedDate() != null) {
            history.add(OrderStatusHistoryDTO.builder()
                    .status(Order.OrderStatus.SHIPPED.name())
                    .statusDisplayName(Order.OrderStatus.SHIPPED.getDisplayName())
                    .timestamp(order.getShippedDate())
                    .notes("Orden enviada")
                    .build());
        }

        // Si está entregada, agregar entrada
        if (order.getDeliveredDate() != null) {
            history.add(OrderStatusHistoryDTO.builder()
                    .status(Order.OrderStatus.DELIVERED.name())
                    .statusDisplayName(Order.OrderStatus.DELIVERED.getDisplayName())
                    .timestamp(order.getDeliveredDate())
                    .notes("Orden entregada")
                    .build());
        }

        // Si está cancelada, agregar entrada
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            history.add(OrderStatusHistoryDTO.builder()
                    .status(Order.OrderStatus.CANCELLED.name())
                    .statusDisplayName(Order.OrderStatus.CANCELLED.getDisplayName())
                    .timestamp(order.getUpdatedAt()) // Usar fecha de última actualización
                    .notes("Orden cancelada")
                    .build());
        }

        return history;
    }


    @Override
    public boolean isValidStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        log.debug("Validando transición de estado: {} -> {}", currentStatus, newStatus);

        return switch (currentStatus) {
            case PENDING -> newStatus == Order.OrderStatus.CONFIRMED ||
                    newStatus == Order.OrderStatus.CANCELLED;

            case CONFIRMED -> newStatus == Order.OrderStatus.SHIPPED ||
                    newStatus == Order.OrderStatus.CANCELLED;

            case SHIPPED -> newStatus == Order.OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false; // Estados finales, no se pueden cambiar
        };
    }


    // ========================================================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN Y PROCESAMIENTO
    // ========================================================================

    private Order createOrderEntity(User user, Cart cart, Address shippingAddress, CreateOrderDTO createOrderDTO) {
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalMount());

        order.setShippingStreet(shippingAddress.getStreet());
        order.setShippingCity(shippingAddress.getCity());
        order.setShippingState(shippingAddress.getState());
        order.setShippingPostalCode(shippingAddress.getPostalCode());
        order.setShippingCountry(shippingAddress.getCountry());
        order.setNotes(createOrderDTO.getNotes());

        // Crea los OrderItems a partir de lo CartItems
        cart.getItems().forEach(item -> {
            OrderItem orderItem = new OrderItem(order, item.getProducto(), item.getQuantity());
            order.addItem(orderItem);
        });

        return orderRepository.save(order);
    }

    private void sendOrderConfirmation(Order order) {
        try {
            // La logic de envious real se hace en EmailService, aquí solo se invoca
            emailService.sendOrderConfirmation(order);
        } catch (Exception e) {
            log.error("Fallo el envío de notificación por orden ID: {}", order.getId(), e);
        }
    }

    private List<CartItem> validateGuestCartItems(List<GuestCartItemDTO> cartItems) {
        // Implementación similar a validateAndReserveStock pera para items de invitado
        return cartItems.stream()
                .map(this::createCartItemFromGuestDTO)
                .toList();
    }

    private CartItem createCartItemFromGuestDTO(GuestCartItemDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getProductId()));
        return new CartItem(producto, dto.getQuantity());
    }

    private void validateAndReserveStockForGuestItems(List<CartItem> items) {
        stockManager.reserveStock(new Cart() {{
            setItems(items);
        }});
    }

    private Order createGuestOrder(CreateGuestOrderDTO dto, List<CartItem> items) {
        Order order = new Order();
        order.setGuestEmail(dto.getGuestEmail());
        order.setGuestFirstName(dto.getGuestFirstName());
        order.setGuestLastName(dto.getGuestLastName());

        // Calcular el total
        BigDecimal total = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // Información de envío
        GuestShippingAddressDTO addr = dto.getGuestShippingAddressDTO();
        order.setShippingStreet(addr.getStreet());
        order.setShippingCity(addr.getCity());
        order.setShippingState(addr.getState());
        order.setShippingPostalCode(addr.getPostalCode());
        order.setShippingCountry(addr.getCountry());
        order.setNotes(dto.getNotes());

        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem(order, item.getProducto(), item.getQuantity());
            order.addItem(orderItem);
        }
        return orderRepository.save(order);
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus,
                                          Order.OrderStatus newStatus) {
        // Lógica de validación de transiciones de estado
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == Order.OrderStatus.CONFIRMED ||
                    newStatus == Order.OrderStatus.CANCELLED;

            case CONFIRMED -> newStatus == Order.OrderStatus.SHIPPED ||
                    newStatus == Order.OrderStatus.CANCELLED;

            case SHIPPED -> newStatus == Order.OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> true;
        };

        if (!validTransition) {
            throw new BusinessException(
                    String.format("Transición de estado inválida: %s -> %s", currentStatus, newStatus)
            );
        }
    }

    private void restoreStockForCancelledOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                productoService.increaseStock(item.getProducto().getId(), item.getQuantity());
                log.info("Stock de producto restaurado correctamente para el producto con ID: {}",
                        item.getProducto().getId());
            } catch (Exception e) {
                log.error("Error al restaurar el stock de producto con ID: {}",
                        item.getProducto().getId());

                throw new BusinessException("Error al restaurar el stock de producto con ID: "
                        + item.getProducto().getId());
            }
        }
    }
}
