package com.example.springbootecommerce.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewDTO {

    @NotNull(message = "El producto es obligatorio")
    private Long productId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String title;

    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
}
