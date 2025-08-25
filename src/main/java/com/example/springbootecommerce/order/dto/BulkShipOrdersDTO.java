package com.example.springbootecommerce.order.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkShipOrdersDTO {
    @NotEmpty(message = "La lista de IDs de órdenes no puede estar vacía")
    private List<Long> orderIds;

}
