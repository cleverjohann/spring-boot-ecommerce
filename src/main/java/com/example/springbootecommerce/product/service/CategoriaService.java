package com.example.springbootecommerce.product.service;

import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.dto.CreateCategoriaDTO;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Interfaz del servicio de categorías que define las operaciones de negocio.
 * Centraliza la lógica de gestión de categorías y facilita la extensión y el mantenimiento.
 */
public interface CategoriaService {
    // ========================================================================
    // OPERACIONES DE CONSULTA
    // ========================================================================
    /**
     * Obtiene todas las categorías.
     *
     * @return Lista de CategoriaDTO
     */
    List<CategoriaDTO> getAllCategories();

    /**
     * Obtiene las categorías raíz (sin padre).
     *
     * @return Lista de CategoriaDTO
     */
    List<CategoriaDTO> getRootCategories();

    /**
     * Obtiene el árbol jerárquico de categorías.
     *
     * @return Lista de CategoriaTreeDTO representando el árbol
     */
    List<CategoriaTreeDTO> getCategoryTree();

    /**
     * Obtiene una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return CategoriaDTO correspondiente
     * @throws ResourceNotFoundException si no existe la categoría
     */
    CategoriaDTO getCategoryById(Long id);

    /**
     * Obtiene las subcategorías de una categoría padre.
     *
     * @param parentId ID de la categoría padre
     * @return Lista de CategoriaDTO de las subcategorías
     * @throws ResourceNotFoundException si no existe la categoría padre
     */
    List<CategoriaDTO> getSubcategories(Long parentId);

    /**
     * Busca categorías por nombre o descripción.
     *
     * @param searchTerm Término de búsqueda
     * @return Lista de CategoriaDTO que coinciden
     */
    List<CategoriaDTO> searchCategories(String searchTerm);

    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    /**
     * Crea una nueva categoría.
     *
     * @param createCategoriaDTO DTO con los datos de la nueva categoría
     * @return CategoriaDTO creada
     * @throws BusinessException si ya existe una categoría con el mismo nombre
     * @throws ResourceNotFoundException si la categoría padre no existe
     */
    CategoriaDTO createCategory(CreateCategoriaDTO createCategoriaDTO);

    /**
     * Actualiza una categoría existente.
     *
     * @param id ID de la categoría a actualizar
     * @param updateCategoriaDTO DTO con los datos actualizados
     * @return CategoriaDTO actualizada
     * @throws BusinessException si ya existe otra categoría con el mismo nombre o si se crea un ciclo
     * @throws ResourceNotFoundException si la categoría o la categoría padre no existen
     */
    CategoriaDTO updateCategory(Long id, CategoriaDTO updateCategoriaDTO);

    /**
     * Elimina (soft delete) una categoría.
     *
     * @param id ID de la categoría a eliminar
     * @throws BusinessException sí tiene productos o subcategorías activas asociadas
     * @throws ResourceNotFoundException si la categoría no existe
     */
    void deleteCategory(Long id);
}
