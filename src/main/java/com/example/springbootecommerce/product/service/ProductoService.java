package com.example.springbootecommerce.product.service;

import com.example.springbootecommerce.product.dto.*;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interfaz del servicio de productos que define las operaciones de negocio.
 * Centraliza la lógica de gestión de productos y facilita la extensión y el mantenimiento.
 */
public interface ProductoService {

    // ========================================================================
    // OPERACIONES DE CONSULTA
    // ========================================================================

    /**
     * Busca productos según criterios avanzados y paginados.
     *
     * @param criteria Criterios de búsqueda
     * @return Página de resúmenes de productos
     */
    Page<ProductoSummaryDTO> searchProducts(ProductoSearchCriteria criteria);

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return ProductDTO correspondiente
     * @throws ResourceNotFoundException si no existe el producto
     */
    ProductoDTO getProductById(Long id);

    /**
     * Obtiene un producto por su SKU.
     *
     * @param sku SKU del producto
     * @return ProductDTO correspondiente
     * @throws ResourceNotFoundException si no existe el producto
     */
    ProductoDTO getProductBySku(String sku);

    /**
     * Obtiene productos de una categoría específica, paginados y ordenados.
     *
     * @param categoryId   ID de la categoría
     * @param page         Página solicitada
     * @param size         Tamaño de página
     * @param sortBy       Campo de ordenamiento
     * @param sortDirection Dirección de ordenamiento (ASC/DESC)
     * @return Página de resúmenes de productos
     * @throws ResourceNotFoundException si la categoría no existe
     */
    Page<ProductoSummaryDTO> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection);

    /**
     * Obtiene todos los productos activos, paginados y ordenados.
     *
     * @param page         Página solicitada
     * @param size         Tamaño de página
     * @param sortBy       Campo de ordenamiento
     * @param sortDirection Dirección de ordenamiento (ASC/DESC)
     * @return Página de resúmenes de productos
     */
    Page<ProductoSummaryDTO> getActiveProducts(int page, int size, String sortBy, String sortDirection);

    /**
     * Obtiene productos relacionados a uno dado.
     *
     * @param productId ID del producto base
     * @param limit     Límite de productos relacionados
     * @return Lista de resúmenes de productos relacionados
     * @throws ResourceNotFoundException si el producto no existe
     */
    List<ProductoSummaryDTO> getRelatedProducts(Long productId, int limit);

    /**
     * Obtiene los productos más vendidos.
     *
     * @param page Página solicitada
     * @param size Tamaño de página
     * @return Página de resúmenes de productos
     */
    Page<ProductoSummaryDTO> getBestSellingProducts(int page, int size);

    /**
     * Obtiene los productos mejor valorados.
     *
     * @param page Página solicitada
     * @param size Tamaño de página
     * @return Página de resúmenes de productos
     */
    Page<ProductoSummaryDTO> getTopRatedProducts(int page, int size);

    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    /**
     * Crea un nuevo producto.
     *
     * @param createProductDTO DTO con los datos del producto
     * @return ProductDTO creado
     * @throws BusinessException si el SKU ya existe
     * @throws ResourceNotFoundException si la categoría no existe
     */
    ProductoDTO createProduct(CreateProductoDTO createProductDTO);

    /**
     * Actualiza un producto existente.
     *
     * @param id               ID del producto a actualizar
     * @param updateProductDTO DTO con los datos a actualizar
     * @return ProductDTO actualizado
     * @throws ResourceNotFoundException si el producto o la categoría no existen
     */
    ProductoDTO updateProduct(Long id, UpdateProductoDTO updateProductDTO);

    /**
     * Elimina (soft delete) un producto.
     *
     * @param id ID del producto a eliminar
     * @throws ResourceNotFoundException si el producto no existe
     */
    void deleteProduct(Long id);

    // ========================================================================
    // GESTIÓN DE INVENTARIO
    // ========================================================================

    /**
     * Actualiza el stock de un producto a un valor específico.
     *
     * @param productId ID del producto
     * @param quantity  Nueva cantidad de stock
     * @throws ResourceNotFoundException si el producto no existe
     * @throws BusinessException si la cantidad es negativa
     */
    void updateStock(Long productId, Integer quantity);

    /**
     * Reduce el stock de un producto en una cantidad dada.
     *
     * @param productId ID del producto
     * @param quantity  Cantidad a reducir
     * @throws ResourceNotFoundException si el producto no existe
     * @throws BusinessException si el stock es insuficiente
     */
    void reduceStock(Long productId, Integer quantity);

    /**
     * Incrementa el stock de un producto en una cantidad dada.
     *
     * @param productId ID del producto
     * @param quantity  Cantidad a incrementar
     * @throws ResourceNotFoundException si el producto no existe
     */
    void increaseStock(Long productId, Integer quantity);

    /**
     * Obtiene productos con bajo stock.
     *
     * @return Lista de resúmenes de productos con bajo stock
     */
    List<ProductoSummaryDTO> getLowStockProducts();

    /**
     * Obtiene productos que necesitan reposición según un umbral.
     *
     * @param threshold Umbral de stock
     * @return Lista de resúmenes de productos
     */
    List<ProductoSummaryDTO> getProductsNeedingRestock(int threshold);
}
