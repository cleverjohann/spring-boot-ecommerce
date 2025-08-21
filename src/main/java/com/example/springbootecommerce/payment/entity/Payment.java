package com.example.springbootecommerce.payment.entity;

import com.example.springbootecommerce.order.entity.Order;
import com.example.springbootecommerce.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "payments")
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_gateway", nullable = false)
    private String paymentGateway;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "gateway_payment_id", nullable = false)
    private String gatewayPaymentId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD" ;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "gateway_response", columnDefinition = "JSON")
    private String gatewayResponse;

    @Column(name = "failure_reason",columnDefinition = "TEXT")
    private String failureReason;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
        this.currency = "USD";
    }

    // ========================================================================
    // MÉTODOS DE NEGOCIO
    // ========================================================================

    public boolean isSuccessful(){
        return status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed(){
        return status == PaymentStatus.FAILED;
    }

    public boolean isPending(){
        return status == PaymentStatus.PENDING;
    }

    public void markAsSuccess(){
        this.status = PaymentStatus.SUCCESS;
        this.processedDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason){
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedDate = LocalDateTime.now();
    }

    public void markAsRefunded(){
        this.status = PaymentStatus.REFUNDED;
        this.processedDate = LocalDateTime.now();
    }

    @Getter
    public enum PaymentStatus {
        PENDING("Pendiente"),
        SUCCESS("Exitoso"),
        FAILED("Fallido"),
        REFUNDED("Reembolsado");

        private final String displayName;

        PaymentStatus(String displayName){
            this.displayName = displayName;
        }
    }
}
