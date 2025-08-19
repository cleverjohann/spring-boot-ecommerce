package com.example.springbootecommerce.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductoDTO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private String sku;

    private Integer stockQuantity;

    private String stockStatus;

    private CategoriaDTO categoria;

    private String imageUrl;

    private BigDecimal weight;

    private String dimension;

    private String brand;

    private Boolean isActive;

    private Double averageRating;

    private Integer totalReviews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ReviewDTO> recentReviews;
}
