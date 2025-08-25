package com.example.springbootecommerce.product.service.impl;

import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.dto.CreateCategoriaDTO;
import com.example.springbootecommerce.product.entity.Categoria;
import com.example.springbootecommerce.product.mapper.CategoriaMapper;
import com.example.springbootecommerce.product.repository.CategoriaRepository;
import com.example.springbootecommerce.product.service.CategoriaService;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;


    @Override
    public List<CategoriaDTO> getAllCategories() {
        log.debug("Obteniendo todas las categories");

        List<Categoria> categories = categoriaRepository.findAll();
        return categories.stream()
                .map(categoriaMapper::toCategoriaDTO)
                .toList();
    }

    @Override
    public List<CategoriaDTO> getRootCategories() {
        log.debug("Obteniendo categories raiz");

        List<Categoria> categories = categoriaRepository.findRootCategories();
        return categories.stream()
                .map(categoriaMapper::toCategoriaDTO)
                .toList();
    }

    @Override
    public List<CategoriaTreeDTO> getCategoryTree() {
        log.debug("Obteniendo tree de categories");
        List<Categoria> categories = categoriaRepository.findRootCategories()   ;
        return categories.stream()
                .map(this::buildCategoryTree)
                .toList();
    }

    @Override
    public CategoriaDTO getCategoryById(Long id) {
        log.debug("Obteniendo category con id {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe categoria con id: " + id));
        return categoriaMapper.toCategoriaDTO(categoria);
    }

    @Override
    public List<CategoriaDTO> getSubcategories(Long parentId) {
        log.debug("Obteniendo subcategories de category con id {}", parentId);

        // Verificar que la categoria exista
        if (!categoriaRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("No existe categoria con id: " + parentId);
        }
        List<Categoria> subcategories = categoriaRepository.findByParentId(parentId);
        return subcategories.stream()
                .map(categoriaMapper::toCategoriaDTO)
                .toList();
    }

    @Override
    public List<CategoriaDTO> searchCategories(String searchTerm) {
        log.debug("Buscando categories con termino: {}", searchTerm);

        List<Categoria> categorias = categoriaRepository.searchByNameOrDescription(searchTerm);
        return categorias.stream()
                .map(categoriaMapper::toCategoriaDTO)
                .toList();
    }
    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    @Transactional
    public CategoriaDTO createCategory(CreateCategoriaDTO createCategoriaDTO) {
        log.info("Creando categoria con nombre: {}", createCategoriaDTO.getName());

        // Verificar que no existe una categoria con el mismo nombre
        if (categoriaRepository.findByNameIgnoreCase(createCategoriaDTO.getName()).isPresent()) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + createCategoriaDTO.getName());
        }

        Categoria categoria = categoriaMapper.toEntity(createCategoriaDTO);

        // Si tiene categoria padre, verificar que existe
        if (createCategoriaDTO.getParentId() != null) {
            Categoria parent = categoriaRepository.findById(createCategoriaDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("No existe categoria padre con id: " + createCategoriaDTO.getParentId()));
            categoria.setParent(parent);
        }
        categoria.setIsActive(true);
        Categoria savedCategoria = categoriaRepository.save(categoria);

        log.info("Categoria creada con ID: {}", savedCategoria.getId());

        return categoriaMapper.toCategoriaDTO(savedCategoria);
    }

    @Transactional
    public CategoriaDTO updateCategory(Long id, CategoriaDTO updateCategoriaDTO) {
        log.info("Actualizando categoria con ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe categoria con id: " + id));

        // Verificar que no existe una categoria con el mismo nombre
        categoriaRepository.findByNameIgnoreCase(updateCategoriaDTO.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new BusinessException("Ya existe otra categoría con el nombre: " + updateCategoriaDTO.getName());
                    }
                });
        // Actualizar los datos
        categoria.setName(updateCategoriaDTO.getName());
        categoria.setDescription(updateCategoriaDTO.getDescription());
        categoria.setDisplayOrder(updateCategoriaDTO.getDisplayOrder());

        // Actualizar la categoria padre si es necesario
        if (updateCategoriaDTO.getParentId() != null) {
            if (updateCategoriaDTO.getParentId().equals(id)){
                throw new BusinessException("No se puede modificar la categoria con id: " + id +
                        " a la misma categoria padre.");
            }
            Categoria parent = categoriaRepository.findById(updateCategoriaDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("No existe categoria padre con id: " + updateCategoriaDTO.getParentId()));

            // Verificar que no se haya creado un ciclo
            if (wouldCreateCycle(categoria, parent)) {
                throw new BusinessException("No se puede modificar la categoria con id: " + id +
                        " a la categoria padre con id: " + updateCategoriaDTO.getParentId() +
                        " porque crea un ciclo.");
            }
            categoria.setParent(parent);
        }else {
            categoria.setParent(null);
        }

        Categoria updateCategoria = categoriaRepository.save(categoria);
        log.info("Categoria actualizada con ID: {}", updateCategoria.getId());
        return categoriaMapper.toCategoriaDTO(updateCategoria);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Eliminando categoria con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe categoria con id: " + id));

        //Verificar que no tiene productos activos
        long productoCount = categoria.getProductos().stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getIsActive()))
                .count();

        if (productoCount > 0) {
            throw new BusinessException("No se puede modificar la categoria con id: " + id +
                    " porque tiene productos activos.");
        }

        // Verificar que no tiene subcategories activas
        long subcategoriasCount = categoria.getSubcategories().stream()
                .filter(subcat -> Boolean.TRUE.equals(subcat.getIsActive()))
                .count();

        if (subcategoriasCount > 0) {
            throw new BusinessException("No se puede modificar la categoria con id: " + id +
                    " porque tiene subcategories activas.");
        }
        categoria.setIsActive(false);
        categoriaRepository.save(categoria);
        log.info("Categoria marcada como inactiva ");
    }

    // ========================================================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================================================

    private CategoriaTreeDTO buildCategoryTree(Categoria categoria) {
        CategoriaTreeDTO treeDTO = categoriaMapper.toCategoriaTreeDTO(categoria);

        List<CategoriaTreeDTO> subcategoriesTree = categoria.getSubcategories().stream()
                .filter(subcat -> Boolean.TRUE.equals(subcat.getIsActive()))
                .map(this::buildCategoryTree)
                .toList();
        treeDTO.setSubcategories(subcategoriesTree);
        return treeDTO;
    }

    private boolean wouldCreateCycle(Categoria categoria, Categoria potentialParent) {
        Categoria current = potentialParent;
        while (current != null) {
            if (current.equals(categoria)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
