package com.example.springbootecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class UpdateProductoDTO {
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio inválido")
    private BigDecimal price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockQuantity;

    private Long categoryId;

    @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
    private String imageUrl;

    @DecimalMin(value = "0.001", message = "El peso debe ser mayor a 0")
    @Digits(integer = 5, fraction = 3, message = "Formato de peso inválido")
    private BigDecimal weight;

    @Size(max = 100, message = "Las dimensiones no pueden exceder 100 caracteres")
    private String dimensions;

    @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
    private String brand;

    private Boolean isActive;
}
