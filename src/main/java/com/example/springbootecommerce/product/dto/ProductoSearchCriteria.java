package com.example.springbootecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoSearchCriteria {
    private String searchTerm;

    private Long categoryId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String brand;

    private Boolean inStock;

    private String sortBy = "name"; // name, price, rating, createdAt

    private String sortDirection = "asc"; // asc, desc

    private Integer page = 0;

    private Integer size = 20;
}
