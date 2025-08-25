package com.example.springbootecommerce.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryDTO {
    private String status;
    private String statusDisplayName;
    private LocalDateTime timestamp;
    private String notes;

}
