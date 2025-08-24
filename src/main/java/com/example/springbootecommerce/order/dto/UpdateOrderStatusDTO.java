package com.example.springbootecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {
    @NotBlank(message = "El estado es obligatorio")
    private String status;

    private String notes;
}
