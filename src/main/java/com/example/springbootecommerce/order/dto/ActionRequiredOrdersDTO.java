package com.example.springbootecommerce.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActionRequiredOrdersDTO {
    private List<OrderSummaryDTO> readyToShipOrders;
    private List<OrderSummaryDTO> pendingDeliveryOrders;
    private List<OrderSummaryDTO> pendingConfirmationOrders;
    private int totalRequiringAction;
}
