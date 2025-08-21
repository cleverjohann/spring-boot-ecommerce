package com.example.springbootecommerce.order.entity;

import com.example.springbootecommerce.payment.entity.Payment;
import com.example.springbootecommerce.shared.audit.Auditable;
import com.example.springbootecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "orders")
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "guest_email", nullable = false)
    private String guestEmail;

    @Column(name = "guest_first_name", nullable = false)
    private String guestFirstName;

    @Column(name = "guest_last_name", nullable = false)
    private String guestLastName;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "shipping_street", nullable = false)
    private String shippingStreet;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    public Order(){
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    // ========================================================================
    // MÉTODOS DE NEGOCIO
    // ========================================================================

    public boolean isGuestOrder(){
        return Objects.isNull(user) && Objects.nonNull(guestEmail);
    }

    public String getCustomerName(){
        if (Objects.nonNull(user)){
            return user.getFullName();
        }else if (Objects.nonNull(guestFirstName) && Objects.nonNull(guestLastName)){
            return guestFirstName + " " + guestLastName;
        }
        return "Cliente Anónimo";
    }

    public String getCustomerEmail(){
        if (Objects.nonNull(user)){
            return user.getEmail();
        }else {
            return Objects.nonNull(guestEmail) ? guestEmail : "";
        }
    }

    public String getShippingAddress(){
        return String.format("%s, %s, %s, %s, %s",
                shippingStreet, shippingCity, shippingState, shippingPostalCode, shippingCountry);
    }

    public void addItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item){
        items.remove(item);
        item.setOrder(null);
    }

    public int getTotalItems(){
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public int getTotalUniqueItems(){
        return items.size();
    }

    public boolean canBeCancelled(){
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean canBeShipped(){
        return status == OrderStatus.CONFIRMED;
    }

    public boolean canBeDelivered(){
        return status == OrderStatus.SHIPPED;
    }

    public void markAsConfirmed(){
        this.status = OrderStatus.CONFIRMED;
    }

    public void markAsShipped(){
        this.status = OrderStatus.SHIPPED;
        this.shippedDate = LocalDateTime.now();
    }

    public void markAsDelivered(){
        this.status = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
    }

    public void markAsCancelled(){
        this.status = OrderStatus.CANCELLED;
    }

    @Getter
    public enum OrderStatus{
        PENDING("Pendiente"),
        CONFIRMED("Confirmada"),
        SHIPPED("Enviada"),
        DELIVERED("Entregada"),
        CANCELLED("Cancelada");

        private final String displayName;

        OrderStatus(String displayName){
            this.displayName = displayName;
        }
    }
}
