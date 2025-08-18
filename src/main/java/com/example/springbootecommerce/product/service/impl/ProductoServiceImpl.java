package com.example.springbootecommerce.product.service.impl;

import com.example.springbootecommerce.product.dto.*;
import com.example.springbootecommerce.product.mapper.ProductoMapper;
import com.example.springbootecommerce.product.repository.CategoriaRepository;
import com.example.springbootecommerce.product.repository.ProductoRepository;
import com.example.springbootecommerce.product.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return null;
    }

    @Override
    public ProductoDTO getProductById(Long id) {
        return null;
    }

    @Override
    public ProductoDTO getProductBySku(String sku) {
        return null;
    }

    @Override
    public Page<ProductoSummaryDTO> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection) {
        return null;
    }

    @Override
    public Page<ProductoSummaryDTO> getActiveProducts(int page, int size, String sortBy, String sortDirection) {
        return null;
    }

    @Override
    public List<ProductoSummaryDTO> getRelatedProducts(Long productId, int limit) {
        return List.of();
    }

    @Override
    public Page<ProductoSummaryDTO> getBestSellingProducts(int page, int size) {
        return null;
    }

    @Override
    public Page<ProductoSummaryDTO> getTopRatedProducts(int page, int size) {
        return null;
    }

    @Override
    public ProductoDTO createProduct(CreateProductoDTO createProductDTO) {
        return null;
    }

    @Override
    public ProductoDTO updateProduct(Long id, UpdateProductoDTO updateProductDTO) {
        return null;
    }

    @Override
    public void deleteProduct(Long id) {

    }

    @Override
    public void updateStock(Long productId, Integer quantity) {

    }

    @Override
    public void reduceStock(Long productId, Integer quantity) {

    }

    @Override
    public void increaseStock(Long productId, Integer quantity) {

    }

    @Override
    public List<ProductoSummaryDTO> getLowStockProducts() {
        return List.of();
    }

    @Override
    public List<ProductoSummaryDTO> getProductsNeedingRestock(int threshold) {
        return List.of();
    }
}
