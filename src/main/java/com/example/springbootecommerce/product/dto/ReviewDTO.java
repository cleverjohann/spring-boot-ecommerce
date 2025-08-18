package com.example.springbootecommerce.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {

    private Long id;

    private Long productId;

    private String productName;

    private String userName;

    private Integer rating;

    private String title;

    private String comment;

    private Boolean isVerifiedPurchase;

    private LocalDateTime createdAt;
}
