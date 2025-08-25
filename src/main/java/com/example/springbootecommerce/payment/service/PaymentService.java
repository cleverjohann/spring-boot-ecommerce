package com.example.springbootecommerce.payment.service;

import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.payment.entity.Payment;

/**
 * Servicio para gestionar el procesamiento de pagos en el sistema.
 */
public interface PaymentService {
    /**
     * Procesa el pago de una orden usando el método de pago especificado.
     *
     * @param order         La orden a pagar.
     * @param paymentMethod El methodo de pago (ej: CREDIT_CARD, PAYPAL, CASH_ON_DELIVERY).
     */
    void processPayment(Order order, String paymentMethod);

    /**
     * Verifica si un pago fue realizado exitosamente.
     * @param paymentId ID del pago a verificar.
     * @return true si el pago fue exitoso, false en caso contrario.
     */
    boolean verifyPayment(Long paymentId);

    /**
     * Procesa el reembolso de un pago existente.
     * @param paymentId ID del pago original a reembolsar.
     * @return El registro del pago de reembolso generado.
     */
    Payment processRefund(Long paymentId);
}
