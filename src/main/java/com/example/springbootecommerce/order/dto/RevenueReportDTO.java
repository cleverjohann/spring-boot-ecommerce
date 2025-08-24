package com.example.springbootecommerce.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueReportDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalRevenue;
    private BigDecimal deliveredRevenue;
    private BigDecimal shippedRevenue;
    private int totalOrders;
    private int deliveredOrders;
    private int shippedOrders;
    private BigDecimal averageOrderValue;
}
