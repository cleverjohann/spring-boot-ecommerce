package com.example.springbootecommerce.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoSku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String productoImageUrl;
    private String isAvailable;
    private String availableStock;
    private LocalDateTime addedAt;
}
