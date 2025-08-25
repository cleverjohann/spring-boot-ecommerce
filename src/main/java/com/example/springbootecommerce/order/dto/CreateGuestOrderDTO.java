package com.example.springbootecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateGuestOrderDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String guestEmail;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String guestFirstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String guestLastName;

    @NotNull(message = "La dirección es obligatoria")
    @Valid
    private GuestShippingAddressDTO guestShippingAddressDTO;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notes;

    @NotBlank(message = "El método es obligatorio")
    private String paymentMethod;

    @NotEmpty(message = "Debe agregar al menos un producto")
    private List<@Valid GuestCartItemDTO> cartItems;
}
