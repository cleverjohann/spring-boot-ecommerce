package com.example.springbootecommerce.order.mapper;

import com.example.springbootecommerce.order.dto.OrderDTO;
import com.example.springbootecommerce.order.dto.OrderItemDTO;
import com.example.springbootecommerce.order.dto.OrderSummaryDTO;
import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.order.entity.OrderItem;
import com.example.springbootecommerce.payment.dto.PaymentDTO;
import com.example.springbootecommerce.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomerName())")
    @Mapping(target = "customerEmail", expression = "java(order.getCustomerEmail())")
    @Mapping(target = "isGuestOrder", expression = "java(order.isGuestOrder())")
    @Mapping(target = "status", expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "shippingAddress", expression = "java(order.getShippingAddress())")
    @Mapping(target = "totalItems", expression = "java(order.getTotalItems())")
    @Mapping(target = "totalUniqueItems", expression = "java(order.getTotalUniqueItems())")
    OrderDTO toOrderDTO (Order order);

    @Mapping(target = "customerName", expression = "java(order.getCustomerName())")
    @Mapping(target = "customerEmail", expression = "java(order.getCustomerEmail())")
    @Mapping(target = "status", expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "totalItems", expression = "java(order.getTotalItems())")
    OrderSummaryDTO toSummaryDTO(Order order);

    @Mapping(target = "productId", source = "producto.id")
    OrderItemDTO toItemDTO(OrderItem item);

    @Mapping(target = "status", expression = "java(payment.getStatus().getDisplayName())")
    PaymentDTO toPaymentDTO(Payment payment);

    List<OrderItemDTO> toItemDTOList(List<OrderItem> items);
}
