package com.example.springbootecommerce.product.mapper;

import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.dto.CreateCategoriaDTO;
import com.example.springbootecommerce.product.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
/**
 * Mapper para conversión entre entidades Categoria y DTO usando MapStruct.
 * Centraliza la lógica de mapeo y facilita la extensión y el mantenimiento.
 */
@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    // =========================================================================
    // CONVERSIONES BÁSICAS CATEGORIA -> DTO
    // =========================================================================
    /**
     * Convierte una entidad Categoria a CategoriaDTO.
     * Incluye información del padre y el conteo de productos activos.
     */
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "productCount", expression = "java(countActiveProductos(categoria))")
    CategoriaDTO toCategoriaDTO (Categoria categoria);

    /**
     * Convierte una lista de entidades Categoria a una lista de CategoriaDTO.
     */
    List<CategoriaDTO> toCategoriaDTOs (List<Categoria> categorias);

    /**
     * Convierte una entidad Categoria a CategoriaTreeDTO (estructura jerárquica).
     * Incluye el conteo de productos activos.
     */
    @Mapping(target = "productCount", expression = "java(countActiveProductos(categoria))")
    @Mapping(target = "subcategories", ignore = true)
    CategoriaTreeDTO toCategoriaTreeDTO(Categoria categoria);

    // =========================================================================
    // CONVERSIONES DTO -> ENTITY
    // =========================================================================
    /**
     * Convierte un CategoriaDTO a una entidad Categoria.
     * Ignora campos que no deben ser mapeados directamente.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "productos", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Categoria toEntity(CreateCategoriaDTO createCategoriaDTO);

    // ========================================================================
    // MÉTODOS AUXILIAR PARA CONTAR PRODUCTOS ACTIVOS
    // ========================================================================
    /**
     * Cuenta la cantidad de productos activos en una categoría.
     * Se usa para poblar el campo productCount en los DTOs.
     */

    default int countActiveProductos(Categoria categoria){
        if (categoria == null || categoria.getProductos() == null) return 0;
        return (int) categoria.getProductos().stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getIsActive()))
                .count();
    }
}
