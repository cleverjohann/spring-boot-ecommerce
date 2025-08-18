package com.example.springbootecommerce.product.repository;

import com.example.springbootecommerce.product.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Encontrar reseñas por producto
    @Query("SELECT r FROM Review r WHERE r.producto.id = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);

    // Encontrar reseñas por usuario
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    Page<Review> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // Verificar si un usuario ya reseñó un producto
    boolean existsByProductoIdAndUserId(Long productId, Long userId);

    // Encontrar reseña específica de usuario y producto
    Optional<Review> findByProductoIdAndUserId(Long productId, Long userId);

    // Reseñas por rating
    @Query("SELECT r FROM Review r WHERE r.producto.id = :productId AND r.rating = :rating ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRating(@Param("productId") Long productId,
                                          @Param("rating") Integer rating,
                                          Pageable pageable);

    // Solo reseñas verificadas (de compradores)
    @Query("SELECT r FROM Review r WHERE r.producto.id = :productId AND r.isVerifiedPurchase = true ORDER BY r.createdAt DESC")
    Page<Review> findVerifiedReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // Estadísticas de rating por producto
    @Query("""
        SELECT r.rating, COUNT(r) 
        FROM Review r 
        WHERE r.producto.id = :productId 
        GROUP BY r.rating 
        ORDER BY r.rating DESC
        """)
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);

    // Rating promedio por producto
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.producto.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    // Contar total de reseñas por producto
    @Query("SELECT COUNT(r) FROM Review r WHERE r.producto.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    // Reseñas más útiles (si tuviéramos sistema de votos)
    @Query("SELECT r FROM Review r WHERE r.producto.id = :productId AND LENGTH(r.comment) > 50 ORDER BY r.createdAt DESC")
    Page<Review> findDetailedReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // Últimas reseñas de un usuario
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findLatestReviewsByUserId(@Param("userId") Long userId, Pageable pageable);
}
