package com.example.springbootecommerce.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {

    private Long id;
    private String userEmail;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer totalUniqueItems;
    private LocalDateTime updatedAt;
}
