package com.example.springbootecommerce.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta estándar para todas las APIs.
 * Proporciona consistencia en las respuestas del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    //Constructor para respuestas existosas
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                "Operation successful",
                data,
                LocalDateTime.now(),
                null
        );
    }

    public static <T> ApiResponse<T> success(T data,String message) {
        return new ApiResponse<>(
                true,
                message,
                data,
                LocalDateTime.now(),
                null
        );
    }

    //Constructor para respuestas de error
    public static <T> ApiResponse<T> error (String message){
        return new ApiResponse<>(
                false,
                message,
                null,
                LocalDateTime.now(),
                null
        );
    }

    public static <T> ApiResponse<T> error (String message,String path){
        return new ApiResponse<>(
                false,
                message,
                null,
                LocalDateTime.now(),
                path
        );
    }
}
