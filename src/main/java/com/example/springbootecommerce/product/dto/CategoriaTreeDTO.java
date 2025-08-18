package com.example.springbootecommerce.product.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoriaTreeDTO {
    private Long id;

    private String name;

    private String description;

    private Integer displayOrder;

    private Integer productCount;

    private List<CategoriaTreeDTO> subcategories = new ArrayList<>();
}
