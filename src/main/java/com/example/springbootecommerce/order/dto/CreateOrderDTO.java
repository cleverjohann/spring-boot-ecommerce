package com.example.springbootecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderDTO {
    @NotNull(message = "La dirección de envío es obligatoria")
    private Long shippingAddressId;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notes;

    @NotBlank(message = "El método de pago es obligatorio")
    private String paymentMethod;
}
