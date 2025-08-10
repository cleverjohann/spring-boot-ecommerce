package com.example.springbootecommerce.shared.exception;

import lombok.Getter;

/**
 * Excepción base para errores de lógica de negocio.
 * Extiende RuntimeException para no requerir manejo explícito.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message){
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode){
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause){
        super(message,cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode, Throwable cause){
        super(message,cause);
        this.errorCode = errorCode;
    }
}
