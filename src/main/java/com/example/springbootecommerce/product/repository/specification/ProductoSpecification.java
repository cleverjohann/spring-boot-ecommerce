package com.example.springbootecommerce.product.repository.specification;

import com.example.springbootecommerce.product.entity.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductoSpecification {
    public static Specification<Producto> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Producto> nameContains(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Producto> brandContains(String brand) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%");
    }

    public static Specification<Producto> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }

    public static Specification<Producto> categoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoryId);
    }
}
