package com.example.springbootecommerce.order.dto;

import com.example.springbootecommerce.payment.dto.PaymentDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private Boolean isGuestOrder;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;
    private Integer totalItems;
    private Integer totalUniqueItems;
}
