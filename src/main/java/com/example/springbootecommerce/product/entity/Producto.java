package com.example.springbootecommerce.product.entity;

import com.example.springbootecommerce.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@FilterDef(name = "activeProductFilter", parameters = @ParamDef(name = "isActive", type = Boolean.class))
@Filter(name = "activeProductFilter", condition = "is_active = :isActive")
public class Producto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false,precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal price;

    @Column(nullable = false, length = 100,unique = true)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String sku;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Categoria categoria;

    @Column(name = "image_url", nullable = false, length = 100)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(length = 100)
    private String dimensions;

    @Column(length = 100)
    private String brand;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // ========================================================================
    // MÉTODOS DE NEGOCIO
    // ========================================================================

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean hasStock(int quantity) {
        return stockQuantity != null && stockQuantity >= quantity;
    }

    public void reduceStock(int quantity) {
        if (!hasStock(quantity)){
            throw new IllegalArgumentException(
                    String.format("Stock insuficiente para el producto %s. Stock actual: %d, solicitado: %d",
                            sku, stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0){
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor a 0");
        }
        this.stockQuantity += quantity;
    }

    public StockStatus getStockStatus(){
        if (stockQuantity == null || stockQuantity <= 0){
            return StockStatus.OUT_OF_STOCK;
        }else if (stockQuantity <= 5){
            return StockStatus.LOW_STOCK;
        }else{
            return StockStatus.IN_STOCK;
        }
    }

    public double getAverageRating(){
        if (reviews.isEmpty()){
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getTotalRatings(){
        return reviews.size();
    }

    @Getter
    public enum StockStatus {
        IN_STOCK("In Stock"),
        LOW_STOCK("Low Stock"),
        OUT_OF_STOCK("Out of Stock");

        private final String displayName;

        StockStatus(String displayName){
            this.displayName = displayName;
        }
    }


}
