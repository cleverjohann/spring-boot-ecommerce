package com.example.springbootecommerce.shared.exception;
/**
 * Excepción para recursos no encontrados.
 * Se lanza cuando se busca una entidad por ID y no existe.
 */
public class ResourceNotFoundException extends BusinessException{

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s: '%s' not found", resourceName, fieldName, fieldValue),
                "RESOURCE_NOT_FOUND");
    }
    public ResourceNotFoundException(String message){
        super(message,"RESOURCE_NOT_FOUND");
    }
}
