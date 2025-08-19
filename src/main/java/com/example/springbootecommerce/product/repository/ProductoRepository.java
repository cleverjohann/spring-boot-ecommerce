package com.example.springbootecommerce.product.repository;

import com.example.springbootecommerce.product.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Long>, JpaSpecificationExecutor<Producto> {

    // Buscar por SKU (único)
    Optional<Producto> findBySku(String sku);

    // Verificar si existe un producto con SKU
    boolean existsBySku(String sku);

    // Productos más vendidos (requiere JOIN con order_items)
//    @Query("""
//        SELECT p FROM Producto p
//        JOIN OrderItem oi ON p.id = oi.product.id
//        JOIN Order o ON oi.order.id = o.id
//        WHERE p.isActive = true AND o.status IN ('DELIVERED', 'SHIPPED')
//        GROUP BY p.id
//        ORDER BY SUM(oi.quantity) DESC
//        """)
//    Page<Producto> findBestSellingProducts(Pageable pageable);

    // Productos mejor calificados
    @Query("""
        SELECT p FROM Producto p 
        LEFT JOIN p.reviews r 
        WHERE p.isActive = true 
        GROUP BY p.id 
        HAVING COUNT(r) > 0 
        ORDER BY AVG(r.rating) DESC
        """)
    Page<Producto> findTopRatedProducts(Pageable pageable);

    // Bloqueo pesimista para manejo de concurrencia en stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :productId")
    Optional<Producto> findByIdWithLock(@Param("productId") Long productId);

    //Productos relacionados por categoria (excluir el producto actual)
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoryId AND p.id <> :excludeProductId")
    List<Producto> findRelatedProducts(@Param("categoryId") Long categoryId,
                                       @Param("excludeProductId") Long excludeProductId,
                                       Pageable pageable);

    // Estadísticas de productos por categoría
    @Query("""
        SELECT c.name, COUNT(p), AVG(p.price), SUM(p.stockQuantity)
        FROM Categoria c 
        LEFT JOIN c.productos p 
        WHERE c.isActive = true AND (p.isActive = true OR p IS NULL)
        GROUP BY c.id, c.name
        """)
    List<Object[]> getProductStatsByCategory();

    // Productos que necesitan restock
    @Query("SELECT p FROM Producto p WHERE p.isActive = true AND p.stockQuantity < :threshold")
    List<Producto> findProductsNeedingRestock(@Param("threshold") int threshold);

    @Query("SELECT p FROM Producto p WHERE p.isActive = true AND p.stockQuantity <= 5")
    List<Producto> findLowStockProducts();


}
