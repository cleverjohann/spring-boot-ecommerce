package com.example.springbootecommerce.payment.exception;

public class PaymentException extends RuntimeException{

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
    }
}
