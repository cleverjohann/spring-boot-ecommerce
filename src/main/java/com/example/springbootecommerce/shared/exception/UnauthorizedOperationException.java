package com.example.springbootecommerce.shared.exception;
/**
 * Excepción para operaciones no autorizadas.
 * Se lanza cuando un usuario intenta realizar una acción sin permisos.
 */
public class UnauthorizedOperationException extends BusinessException{

    public UnauthorizedOperationException(String message) {
        super(message,"UNAUTHORIZED_OPERATION");
    }

    public UnauthorizedOperationException(String operation, String resource) {
        super(String.format("Unauthorized to perform '%s' operation on '%s'", operation, resource),
                "UNAUTHORIZED_OPERATION");
    }
}
