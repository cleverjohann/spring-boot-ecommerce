package com.example.springbootecommerce.product.service.impl;

import com.example.springbootecommerce.product.dto.*;
import com.example.springbootecommerce.product.entity.Categoria;
import com.example.springbootecommerce.product.entity.Producto;
import com.example.springbootecommerce.product.mapper.ProductoMapper;
import com.example.springbootecommerce.product.repository.CategoriaRepository;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import com.example.springbootecommerce.product.repository.specification.ProductoSpecification;
import com.example.springbootecommerce.product.service.ProductoService;
import com.example.springbootecommerce.shared.exception.BusinessException;
import com.example.springbootecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaRepository categoriaRepository;


    @Override
    public Page<ProductoSummaryDTO> searchProducts(ProductoSearchCriteria criteria) {
        log.debug("Buscando productos con criterios: {}", criteria);

        Specification<Producto> spec = createProductSpecification(criteria);
        Pageable pageable = createPage(criteria);

        Page<Producto> productos = productoRepository.findAll(spec, pageable);
        return productos.map(productoMapper::toSummaryDTO);
    }

    @Override
    public ProductoDTO getProductById(Long id) {
        log.debug("Obteniendo producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        return productoMapper.toProductoDTO(producto);
    }

    @Override
    public ProductoDTO getProductBySku(String sku) {
        log.debug("Obteniendo producto con SKU: {}", sku);

        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con SKU: " + sku));

        return productoMapper.toProductoDTO(producto);
    }

    @Override
    public Page<ProductoSummaryDTO> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection) {
        log.debug("Obteniendo productos de categoria ID :{}", categoryId);

        // Verificar que la categoria existe
        if (!categoriaRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoria no encontrada con ID: " + categoryId);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Producto> productos = productoRepository.findAll(ProductoSpecification.categoryId(categoryId), pageable);
        return productos.map(productoMapper::toSummaryDTO);
    }

    @Override
    public Page<ProductoSummaryDTO> getActiveProducts(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Producto> productos = productoRepository.findAll(ProductoSpecification.isActive(), pageable);
        return productos.map(productoMapper::toSummaryDTO);
    }

    @Override
    public List<ProductoSummaryDTO> getRelatedProducts(Long productId, int limit) {
        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        Pageable pageable = PageRequest.of(0, limit);
        List<Producto> relatedProducts = productoRepository.findRelatedProducts(
                producto.getCategoria().getId(), productId, pageable);

        return relatedProducts.stream()
                .map(productoMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public Page<ProductoSummaryDTO> getBestSellingProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productos = productoRepository.findBestSellingProducts(pageable);
        return productos.map(productoMapper::toSummaryDTO);
    }

    @Override
    public Page<ProductoSummaryDTO> getTopRatedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productos = productoRepository.findTopRatedProducts(pageable);
        return productos.map(productoMapper::toSummaryDTO);
    }

    // ========================================================================
    // OPERACIONES ADMINISTRATIVAS
    // ========================================================================

    @Transactional
    public ProductoDTO createProduct(CreateProductoDTO createProductDTO) {
        log.info("Creando producto con SKU: {}", createProductDTO.getSku());

        // Validar que no exista un producto con el SKU
        if (productoRepository.existsBySku(createProductDTO.getSku())) {
            throw new ResourceNotFoundException("Ya existe un producto con SKU: " + createProductDTO.getSku());
        }

        // Verificar que la categoria existe
        Categoria categoria = categoriaRepository.findById(createProductDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " +
                        createProductDTO.getCategoryId()));

        Producto producto = productoMapper.toEntity(createProductDTO);
        producto.setCategoria(categoria);
        producto.setIsActive(true);

        Producto saveProducto = productoRepository.save(producto);

        log.info("Producto creado con ID: {}", saveProducto.getId());
        return productoMapper.toProductoDTO(saveProducto);
    }

    @Transactional
    public ProductoDTO updateProduct(Long id, UpdateProductoDTO updateProductDTO) {
        log.info("Actualizando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (updateProductDTO.getName() != null) producto.setName(updateProductDTO.getName());
        if (updateProductDTO.getDescription() != null) producto.setDescription(updateProductDTO.getDescription());
        if (updateProductDTO.getPrice() != null) producto.setPrice(updateProductDTO.getPrice());
        if (updateProductDTO.getStockQuantity() != null) producto.setStockQuantity(updateProductDTO.getStockQuantity());
        if (updateProductDTO.getCategoryId() != null) {
            Categoria categoria = categoriaRepository.findById(updateProductDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con ID: " +
                            updateProductDTO.getCategoryId()));
            producto.setCategoria(categoria);
        }
        if (updateProductDTO.getImageUrl() != null) producto.setImageUrl(updateProductDTO.getImageUrl());
        if (updateProductDTO.getWeight() != null) producto.setWeight(updateProductDTO.getWeight());
        if (updateProductDTO.getBrand() != null) producto.setBrand(updateProductDTO.getBrand());
        if (updateProductDTO.getIsActive() != null) producto.setIsActive(updateProductDTO.getIsActive());

        Producto updateProducto = productoRepository.save(producto);
        log.info("Producto actualizado con ID: {}", updateProducto.getId());
        return productoMapper.toProductoDTO(updateProducto);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Eliminando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setIsActive(false);
        productoRepository.save(producto);
        log.info("Producto eliminado con ID: {}", id);
    }

    // ========================================================================
    // GESTIÓN DE INVENTARIO
    // ========================================================================

    @Override
    public void updateStock(Long productId, Integer quantity) {
        log.info("Actualizando stock de producto con ID: {}. Nueva cantidad: {}", productId, quantity);

        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        if (quantity < 0) {
            throw new BusinessException("La cantidad debe ser mayor o igual a 0.");
        }
        producto.setStockQuantity(producto.getStockQuantity() + quantity);
        productoRepository.save(producto);

        log.info("Stock actualizado de producto con ID: {}", productId);
    }

    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Reduciendo stock de producto con ID: {}. Cantidad a reducir: {}", productId, quantity);

        //Usar bloqueo pesimista para manejo de concurrencia
        Producto producto = productoRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (!producto.hasStock(quantity)) {
            throw new BusinessException(
                    String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                            producto.getStockQuantity(), quantity));
        }

        producto.reduceStock(quantity);
        productoRepository.save(producto);

        log.info("Stock reducido de producto con ID: {}", productId);
    }

    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        log.info("Aumentando stock de producto con ID: {}. Cantidad a aumentar: {}", productId, quantity);

        Producto producto = productoRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        producto.increaseStock(quantity);
        productoRepository.save(producto);

        log.info("Stock aumentado de producto con ID: {}", productId);

    }

    @Override
    public List<ProductoSummaryDTO> getLowStockProducts() {
        List<Producto> lowStockProducts = productoRepository.findLowStockProducts();
        return lowStockProducts.stream()
                .map(productoMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public List<ProductoSummaryDTO> getProductsNeedingRestock(int threshold) {
        List<Producto> productsNeedingRestock = productoRepository.findProductsNeedingRestock(threshold);
        return productsNeedingRestock.stream()
                .map(productoMapper::toSummaryDTO)
                .toList();
    }
    // ========================================================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================================================================

    private Specification<Producto> createProductSpecification(ProductoSearchCriteria criteria) {
        Specification<Producto> spec = ProductoSpecification.isActive();

        if (StringUtils.hasText(criteria.getSearchTerm())) {
            spec = spec.and(ProductoSpecification.nameContains(criteria.getSearchTerm()));
            // Si quieres buscar también en descripción
        }
        if (criteria.getCategoryId() != null) {
            spec = spec.and(ProductoSpecification.categoryId(criteria.getCategoryId()));
        }
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            spec = spec.and(ProductoSpecification.priceBetween(criteria.getMinPrice(), criteria.getMaxPrice()));
        } else if (criteria.getMinPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        } else if (criteria.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }
        if (StringUtils.hasText(criteria.getBrand())) {
            spec = spec.and(ProductoSpecification.brandContains(criteria.getBrand()));
        }
        if (criteria.getInStock() != null && criteria.getInStock()) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0));
        }
        return spec;
    }

    private Pageable createPage(ProductoSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.getSortDirection());
        Sort sort = Sort.by(direction, criteria.getSortBy());
        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }
}
