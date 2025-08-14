package com.example.springbootecommerce.user.repository;

import java.math.BigDecimal;

public interface UserStatisticsProjection {
    Long getUserId();
    String getEmail();
    String getFirstName();
    String getLastName();
    Long getOrderCount();
    BigDecimal getTotalSpent();
}
