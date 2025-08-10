package com.example.springbootecommerce.shared.exception;

import lombok.Getter;

/**
 * Excepción para casos de stock insuficiente.
 * Se lanza cuando se intenta procesar una orden sin stock disponible.
 */
@Getter
public class InsufficientStockException extends BusinessException {

    private final String productName;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(String productName, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
                        productName, requestedQuantity, availableStock),
                "INSUFFICIENT_STOCK");
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

}
