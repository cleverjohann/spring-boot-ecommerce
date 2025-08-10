package com.example.springbootecommerce.shared.exception;
/**
 * Excepción para recursos duplicados.
 * Se lanza cuando se intenta crear un recurso que ya existe.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s: '%s' already exists", resourceName, fieldName, fieldValue),
                "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message){
        super(message,"DUPLICATE_RESOURCE");
    }
}
