package com.example.springbootecommerce.product.mapper;

import com.example.springbootecommerce.product.dto.ReviewDTO;
import com.example.springbootecommerce.product.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper para conversión entre entidades Review y DTOs usando MapStruct.
 * Centraliza la lógica de mapeo y facilita la extensión y el mantenimiento.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {
    // =========================================================================
    // CONVERSIONES BÁSICAS REVIEW -> DTO
    // =========================================================================
    /**
     * Convierte una entidad Review a ReviewDTO.
     * Incluye campos calculados como productName y userName.
     */
    @Mapping(target = "productId", source = "producto.id")
    @Mapping(target = "productName", source = "producto.name")
    @Mapping(target = "userName", expression = "java(getUserName(review))")
    ReviewDTO toReviewDTO(Review review);

    /**
     * Convierte una lista de entidades Review a una lista de ReviewDTO.
     */
    List<ReviewDTO> toReviewDTOs(List<Review> reviews);

    // =========================================================================
    // CONVERSIONES DTO -> ENTITY
    // =========================================================================
    /**
     * Convierte un CreateReviewDTO a una entidad Review.
     * Los campos product y user se asignan en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isVerifiedPurchase", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewDTO reviewDTO);

    // ========================================================================
    // MÉTODOS AUXILIAR PARA OBTENER EL NOMBRE COMPLETO DEL USUARIO
    // ========================================================================

    /**
     * Obtiene el nombre completo del usuario que hizo la reseña.
     */
    default String getUserName(Review review) {
        if (review == null || review.getUser() == null) return null;
        var user = review.getUser();
        return (user.getFirstName() != null ? user.getFirstName() : "") +
                " " +
                (user.getLastName() != null ? user.getLastName() : "");
    }

}
