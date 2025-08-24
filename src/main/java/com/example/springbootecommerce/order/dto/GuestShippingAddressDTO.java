package com.example.springbootecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestShippingAddressDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotBlank(message = "El estado es obligatorio")
    private String state;

    @NotBlank(message = "El código postal es obligatorio")
    private String postalCode;

    @NotBlank(message = "El pais es obligatorio")
    private String country;
}
