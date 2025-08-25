package com.example.springbootecommerce.order.service;

import com.example.springbootecommerce.order.dto.*;
import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.order.repository.specification.OrderSpecification;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import com.example.springbootecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz del servicio central de gestión de órdenes
 * Define el contrato para el procesamiento completo del ciclo de vida de órdenes
 */

public interface OrderService {

    // ========================================================================
    // PROCESAMIENTO DE ÓRDENES - MÉTODOS TRANSACCIONALES CRÍTICOS
    // ========================================================================
    /**
     * Procesa una orden para un usuario registrado con validación completa
     * Función transaccional crítico que coordina el flujo de e-commerce
     *
     * @param user Usuario autenticado que realiza la orden
     * @param createOrderDTO DTO con datos de la orden (dirección, métodos pago, notas)
     * @return OrderDTO con los datos de la orden procesada
     * @throws BusinessException Sí hay problemas de validación o procesamiento
     */
    OrderDTO placeOrder(User user, CreateOrderDTO createOrderDTO);

    /**
     * Procesa una orden para un usuario invitado sin registro previo
     * Incluye validación de items y procesamiento de pago
     *
     * @param createGuestOrderDTO DTO completo con datos del invitado y orden
     * @return OrderDTO con los datos de la orden procesada
     * @throws BusinessException Si hay problemas de validación o procesamiento
     */
    OrderDTO placeGuestOrder(CreateGuestOrderDTO createGuestOrderDTO);

    // ========================================================================
    // CONSULTAS DE ÓRDENES - MÉTODOS DE LECTURA
    // ========================================================================
    /**
     * Obtiene una orden por ID sin restricciones (uso administrativo)
     * Incluye items y datos de pago mediante JOIN FETCH
     *
     * @param orderId ID de la orden a consultar
     * @return OrderDTO completo con items y pago
     * @throws ResourceNotFoundException Si la orden no existe
     */
    OrderDTO getOrderById(Long orderId);

    /**
     * Obtiene una orden por ID con validación de pertenencia al usuario
     * Solo permite ver la orden si pertenece al usuario o es admin
     *
     * @param orderId ID de la orden a consultar
     * @param user Usuario que solicita la consulta
     * @return OrderDTO completo con validación de pertenencia
     * @throws ResourceNotFoundException Si la orden no existe
     * @throws BusinessException Si el usuario no tiene permisos
     */
    OrderDTO getOrderById(Long orderId, User user);

    /**
     * Obtiene todas las órdenes de un usuario específico paginadas
     * Ordenadas por fecha de orden descendente (más recientes primero)
     *
     * @param user Usuario propietario de las órdenes
     * @param pageable Configuración de paginación
     * @return Page de OrderSummaryDTO para listados eficientes
     */
    Page<OrderSummaryDTO> getUserOrders(User user, Pageable pageable);

    /**
     * Obtiene todas las órdenes del sistema con paginación (uso administrativo)
     *
     * @param pageable Configuración de paginación
     * @return Page de OrderSummaryDTO para administración
     */
    Page<OrderSummaryDTO> getAllOrders(Pageable pageable);

    /**
     * Búsqueda avanzada de órdenes usando especificaciones dinámicas
     * Permite filtros combinados por usuario, estado, fechas, etc.
     *
     * @param spec Especificación JPA construida dinámicamente
     * @param pageable Configuración de paginación
     * @return Page de OrderSummaryDTO filtradas según criterios
     */
    Page<OrderSummaryDTO> searchOrders(Specification<Order> spec, Pageable pageable);

    /**
     * Obtiene órdenes de un usuario filtradas por estado específico
     *
     * @param user Usuario propietario
     * @param status Estado de orden a filtrar
     * @param pageable Configuración de paginación
     * @return Page de órdenes filtradas por estado
     */
    Page<OrderSummaryDTO> getUserOrdersByStatus(User user, Order.OrderStatus status, Pageable pageable);

    /**
     * Obtiene órdenes de invitado por email en un rango de fechas
     * Útil para tracking de órdenes sin registro
     *
     * @param guestEmail Email del invitado
     * @param startDate Fecha inicio del rango
     * @param endDate Fecha fin del rango
     * @param pageable Configuración de paginación
     * @return Page de órdenes de invitado
     */
    Page<OrderSummaryDTO> getGuestOrdersByEmailAndDateRange(String guestEmail,
                                                            LocalDateTime startDate,
                                                            LocalDateTime endDate,
                                                            Pageable pageable);

    // ========================================================================
    // GESTIÓN ADMINISTRATIVA - CAMBIOS DE ESTADO Y SEGUIMIENTO
    // ========================================================================

    /**
     * Actualiza el estado de una orden con validación de transiciones válidas
     * Función transaccional que maneja lógica de negocio según el nuevo estado
     *
     * @param orderId ID de la orden a actualizar
     * @param updateStatusDTO DTO con nuevo estado y notas opcionales
     * @return OrderDTO actualizada
     * @throws ResourceNotFoundException Si la orden no existe
     * @throws BusinessException Si la transición de estado es inválida
     */
    OrderDTO updateOrderStatus(Long orderId, UpdateOrderStatusDTO updateStatusDTO);

    /**
     * Cancela una orden si está en estado válido para cancelación
     * Restaura automáticamente el stock de productos
     *
     * @param orderId ID de la orden a cancelar
     * @param reason Motivo de la cancelación
     * @return OrderDTO de la orden cancelada
     * @throws BusinessException Si la orden no puede ser cancelada
     */
    OrderDTO cancelOrder(Long orderId, String reason);

    /**
     * Marca múltiples órdenes como enviadas en lote
     * Útil para procesamiento masivo de envíos
     *
     * @param orderIds Lista de IDs de órdenes a marcar como enviadas
     * @return Lista de OrderDTO actualizadas
     * @throws BusinessException Si alguna orden no puede ser enviada
     */
    List<OrderDTO> markOrdersAsShipped(List<Long> orderIds);

    // ========================================================================
    // REPORTES Y ESTADÍSTICAS - MÉTODOS DE ANÁLISIS
    // ========================================================================

    /**
     * Genera reporte de ingresos por rango de fechas
     * Solo considera órdenes entregadas y enviadas
     *
     * @param startDate Fecha inicio del período
     * @param endDate Fecha fin del período
     * @return RevenueReportDTO con estadísticas de ingresos
     */
    RevenueReportDTO getRevenuesReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Obtiene estadísticas generales del sistema de órdenes
     * Incluye conteos por estado y totales
     *
     * @return OrderStatsDTO con estadísticas completas
     */
    OrderStatsDTO getOrdersStatistics();

    /**
     * Encuentra órdenes que requieren atención por tiempo transcurrido
     * Órdenes confirmadas listas para envío y enviadas pendientes de entrega
     *
     * @return ActionRequiredOrdersDTO con órdenes que necesitan seguimiento
     */
    ActionRequiredOrdersDTO getOrdersRequiringAction();

    // ========================================================================
    // MÉTODOS DE UTILIDAD Y VALIDACIÓN
    // ========================================================================
    /**
     * Verifica si un usuario puede ver una orden específica
     * Valida pertenencia o permisos administrativos
     *
     * @param orderId ID de la orden
     * @param user Usuario que solicita el acceso
     * @return true si puede acceder, false en caso contrario
     */
    boolean canUserAccessOrder(Long orderId, User user);

    /**
     * Obtiene el historial de cambios de estado de una orden
     * Útil para auditoría y seguimiento detallado
     *
     * @param orderId ID de la orden
     * @return Lista de OrderStatusHistoryDTO con el historial
     */
    List<OrderStatusHistoryDTO> getOrderStatusHistory(Long orderId);

    /**
     * Valida si una transición de estado es válida según las reglas de negocio
     *
     * @param currentStatus Estado actual
     * @param newStatus Nuevo estado propuesto
     * @return true si la transición es válida
     */
    boolean isValidStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus);

}
