package com.example.springbootecommerce.shared.exception;

import lombok.Getter;

/**
 * Excepción para errores de pago.
 * Se lanza cuando hay problemas con el procesamiento de pagos.
 */
@Getter
public class PaymentException extends BusinessException{

    private final String paymentGateway;
    private final String transactionId;

    public PaymentException(String message, String paymentGateway, String transactionId){
        super(message, "PAYMENT_ERROR");
        this.paymentGateway = paymentGateway;
        this.transactionId = transactionId;
    }

    public PaymentException(String message, String paymentGateway, String transactionId, Throwable cause){
        super(message, "PAYMENT_ERROR", cause);
        this.paymentGateway = paymentGateway;
        this.transactionId = transactionId;
    }

}
