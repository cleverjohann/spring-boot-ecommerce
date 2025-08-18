package com.example.springbootecommerce.product.mapper;

import com.example.springbootecommerce.product.dto.CreateProductoDTO;
import com.example.springbootecommerce.product.dto.ProductoDTO;
import com.example.springbootecommerce.product.dto.ProductoSummaryDTO;
import com.example.springbootecommerce.product.dto.ReviewDTO;
import com.example.springbootecommerce.product.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper para conversión entre entidades Producto y DTOs usando MapStruct.
 * Centraliza la lógica de mapeo y facilita la extensión y el mantenimiento.
 */
@Mapper(
        componentModel = "spring",
        uses = { CategoriaMapper.class, ReviewMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductoMapper {

    // =========================================================================
    // CONVERSIONES BÁSICAS PRODUCTO -> DTO
    // =========================================================================
    /**
     * Convierte una entidad Producto a ProductoDTO.
     * Incluye categoría anidada y últimas 3 reseñas.
     */
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "stockStatus", expression = "java(producto.getStockStatus() != null ?" +
            " producto.getStockStatus().getDisplayName() :" +
            " null)" )
    @Mapping(target = "recentReviews", expression = "java(mapRecentReviews(producto))")
    ProductoDTO toProductoDTO(Producto producto);

    /**
     * Convierte una lista de entidades Producto a una lista de ProductoDTO.
     */
    List<ProductoDTO> toProductoDTOs(List<Producto> productos);

    /**
     * Convierte una entidad Producto a ProductoSummaryDTO.
     * Incluye el promedio y total views
     */
    @Mapping(target = "categoryName", source = "categoria.name")
    @Mapping(target = "stockStatus", expression = "java(producto.getStockStatus() != null ?" +
            " producto.getStockStatus().getDisplayName() :" +
            " null)" )
    @Mapping(target = "averageRating", expression = "java(producto.getAverageRating())")
    @Mapping(target = "totalReviews", expression = "java(producto.getTotalRatings())")
    ProductoSummaryDTO toSummaryDTO(Producto producto);

    /**
     * Convierte una lista de entidades Producto a una lista de ProductoSummaryDTO.
     */
    List<ProductoSummaryDTO> toSummaryDTOs(List<Producto> productos);

    // =========================================================================
    // CONVERSIONES DTO -> ENTITY
    // =========================================================================
    /**
     * Convierte un CreateProductoDTO a una entidad Producto.
     * La categoría se asigna en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "reviews",ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    Producto toEntity(CreateProductoDTO createProductoDTO);

    // =========================================================================
    // MÉTODOS AUXILIARES (default methods)
    // =========================================================================
    /**
     * Obtiene las 3 reseñas más recientes del producto.
     */
    default List<ReviewDTO> mapRecentReviews (Producto producto){
        if (producto == null || producto.getReviews() == null || producto.getReviews().isEmpty()){
            return List.of();
        }
        return producto.getReviews().stream()
                .sorted()
                .limit(3)
                .map(getReviewMapper()::toReviewDTO)
                .toList();
    }

    /**
     * Provee acceso al ReviewMapper para métodos default.
     * MapStruct inyecta automáticamente los mappers usados.
     */
    @ObjectFactory
    default ReviewMapper getReviewMapper(){
        //MapStruct inyecta el mapper real en tiempo de ejecución.
        return null;
    }


}
