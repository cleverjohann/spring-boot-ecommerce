package com.example.springbootecommerce.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoSummaryDTO {
    private Long id;

    private String name;

    private String sku;

    private BigDecimal price;

    private Integer stockQuantity;

    private String stockStatus;

    private String categoryName;

    private String brand;

    private String imageUrl;

    private Double averageRating;

    private Integer totalReviews;
}
