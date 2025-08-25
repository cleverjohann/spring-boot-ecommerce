package com.example.springbootecommerce.payment.service.impl;

import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.payment.entity.Payment;
import com.example.springbootecommerce.payment.repository.PaymentRepository;
import com.example.springbootecommerce.payment.service.PaymentService;
import com.example.springbootecommerce.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    @Override
    public void processPayment(Order order, String paymentMethod) {
        log.info("Procesando pago para orden ID: {}, con método: {}", order.getId(), paymentMethod);

        // Crear un registro de pago
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("USD");

        //Simular procesamiento de pago según la función

        payment = switch (paymentMethod.toUpperCase()) {
            case "CREDIT_CARD" -> {
                payment.setPaymentGateway("stripe");
                yield processStripePayment(payment);
            }
            case "PAYPAL" -> {
                payment.setPaymentGateway("paypal");
                yield processPaypalPayment(payment);
            }
            case "CASH_ON_DELIVERY" -> {
                payment.setPaymentGateway("manual");
                yield processCashOnDelivery(payment);
            }
            default -> throw new BusinessException("Método de pago no soportado: " + paymentMethod);
        };

        paymentRepository.save(payment);
    }

    @Override
    public boolean verifyPayment(Long paymentId){
        return paymentRepository.findById(paymentId)
                .map(Payment::isSuccessful)
                .orElse(false);
    }

    @Override
    public Payment processRefund(Long paymentId){
        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Pago no encontrado"));

        if(!originalPayment.isSuccessful()){
            throw new BusinessException("El pago no pudo ser procesado");
        }

        //Crear pago de Reembolso
        Payment refundPayment = new Payment();
        refundPayment.setOrder(originalPayment.getOrder());
        refundPayment.setPaymentMethod(originalPayment.getPaymentMethod());
        refundPayment.setAmount(originalPayment.getAmount());
        refundPayment.setCurrency(originalPayment.getCurrency());
        refundPayment.setTransactionId("REFUND-" + UUID.randomUUID().toString());
        refundPayment.markAsSuccess();

        originalPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(originalPayment);
        return paymentRepository.save(refundPayment);
    }

    // ========================================================================
    // MÉTODOS PRIVADOS DE PROCESAMIENTO
    // ========================================================================

    private Payment processStripePayment(Payment payment) {
        try {
            //TODO aquí iria la integración real con stripe

            payment.setTransactionId("pi_" + UUID.randomUUID().toString());
            payment.setGatewayPaymentId("stripe_" + System.currentTimeMillis());
            //Simular respuesta de gateway
            String gatewayResponse = """
                    {
                        "id": "%s",
                        "status": "succeeded",
                        "amount": %s,
                        "currency": "usd",
                        "created": %s
                    }
                    """.formatted(
                    payment.getGatewayPaymentId(),
                    payment.getAmount().multiply(new BigDecimal("100")).intValue(),
                    System.currentTimeMillis() / 1000
            );
            payment.setGatewayResponse(gatewayResponse);
            payment.markAsSuccess();

            log.debug("Pago Stripe simulado correctamente");
        }catch (Exception e){
            log.error("Error procesando pago Stripe: {}", e.getMessage());
            payment.markAsFailed("Error procesando pago Stripe" + e.getMessage());
        }
        return payment;
    }

    private Payment processPaypalPayment(Payment payment) {
        try {
            //TODO: Aquí iria la integración con paypal

            payment.setTransactionId("PAY-" + UUID.randomUUID().toString());
            payment.setGatewayPaymentId("paypal_" + System.currentTimeMillis());

            String gatewayResponse = """
                {
                    "id": "%s",
                    "state": "approved",
                    "amount": {
                        "total": "%s",
                        "currency": "USD"
                    }
                }
                """.formatted(payment.getGatewayPaymentId(), payment.getAmount());
            payment.setGatewayResponse(gatewayResponse);
            payment.markAsSuccess();

            log.debug("Pago paypal simulado correctamente");
        }catch (Exception e){
            log.error("Error procesando pago paypal: {}", e.getMessage());
            payment.markAsFailed("Error procesando pago paypal" + e.getMessage());
        }
        return payment;
    }

    private Payment processCashOnDelivery(Payment payment){
        // Pago contra entrega queda como pendiente hasta la entrega

        payment.setTransactionId("COD-" + UUID.randomUUID().toString());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        String gatewayResponse = """
            {
                "method": "cash_on_delivery",
                "status": "pending",
                "note": "Payment will be collected upon delivery"
            }
            """;
        payment.setGatewayResponse(gatewayResponse);
        log.debug("Pago contra entrega simulado correctamente");
        return payment;
    }
}
