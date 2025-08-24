package com.example.springbootecommerce.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private String paymentMethod;
    private String paymentGateway;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime paymentDate;
    private LocalDateTime processedDate;
}
