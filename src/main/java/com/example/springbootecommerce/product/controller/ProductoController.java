package com.example.springbootecommerce.product.controller;

import com.example.springbootecommerce.product.dto.*;
import com.example.springbootecommerce.product.service.ProductoService;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import com.example.springbootecommerce.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.example.springbootecommerce.shared.util.Constants.PRODUCTS_ENDPOINT;

@RestController
@RequestMapping(PRODUCTS_ENDPOINT)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Productos", description = "API para gestion de productos")
public class ProductoController {

    private final ProductoService productoService;

    // ========================================================================
    // ENDPOINTS PÚBLICOS (Catálogo)
    // ========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductoSummaryDTO>>> searchProductos(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
            ){
        log.debug("Búsqueda de productos - termino: {}, categoria: {}",searchTerm, categoriaId);

        ProductoSearchCriteria criteria = new ProductoSearchCriteria();
        criteria.setSearchTerm(searchTerm);
        criteria.setCategoryId(categoriaId);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setBrand(brand);
        criteria.setInStock(inStock);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);
        criteria.setPage(page);

        Page<ProductoSummaryDTO> productos = productoService.searchProducts(criteria);

        PageResponse<ProductoSummaryDTO> pageResponse = PageResponse.of(productos);

        return ResponseEntity.ok(ApiResponse.success(pageResponse,"Productos encontrados exitosamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDTO>> getProductoById(@PathVariable Long id){
        log.debug("Obteniendo del id de producto: {}", id);

        ProductoDTO producto = productoService.getProductById(id);

        return ResponseEntity.ok(ApiResponse.success(producto,"Producto encontrado"));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductoDTO>> getProductoBySku(@PathVariable String sku){
        log.debug("Obteniendo del id de sku: {}", sku);

        ProductoDTO producto = productoService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(producto,"Producto encontrado"));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductoSummaryDTO>>> getProductosByCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ){
        log.debug("Obteniendo del id de categoria: {}", categoriaId);

        Page<ProductoSummaryDTO> productos = productoService.getProductsByCategory(categoriaId,
                page, size, sortBy, sortDirection);

        PageResponse<ProductoSummaryDTO> pageResponse = PageResponse.of(productos);
        return ResponseEntity.ok(ApiResponse.success(pageResponse,"Productos encontrados"));
    }

    @GetMapping("/{id}/relacionados")
    public ResponseEntity<ApiResponse<List<ProductoSummaryDTO>>> getProductosRelacionados(
            @PathVariable Long id,
            @RequestParam (defaultValue = "6") int limit
    ){
        log.debug("Obteniendo del id de relacionados: {}", id);

        List<ProductoSummaryDTO> relacionados = productoService.getRelatedProducts(id,limit);
        return ResponseEntity.ok(ApiResponse.success(relacionados,
                "Productos relacionados encontrados exitosamente"));
    }

    @GetMapping("/mas-vendidos")
    public ResponseEntity<ApiResponse<PageResponse<ProductoSummaryDTO>>> getProductosMasVendidos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var productos = productoService.getBestSellingProducts(page, size);
        var pageResponse = PageResponse.of(productos);
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Productos más vendidos encontrados exitosamente"));
    }

    @GetMapping("/mejor-calificados")
    public ResponseEntity<ApiResponse<PageResponse<ProductoSummaryDTO>>> getProductosMejorCalificados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var productos = productoService.getTopRatedProducts(page, size);
        var pageResponse = PageResponse.of(productos);
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Productos mejor calificados encontrados exitosamente"));
    }
    // ========================================================================
    // ENDPOINTS ADMINISTRATIVOS
    // ========================================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoDTO>> crearProducto(
            @Valid @RequestBody CreateProductoDTO createProductoDTO) {
        log.info("Creando nuevo producto: {}", createProductoDTO.getName());
        ProductoDTO creado = productoService.createProduct(createProductoDTO);
        return ResponseEntity.status(201).body(ApiResponse.success(creado, "Producto creado exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoDTO>> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductoDTO updateProductoDTO) {
        log.info("Actualizando producto ID: {}", id);
        ProductoDTO actualizado = productoService.updateProduct(id, updateProductoDTO);
        return ResponseEntity.ok(ApiResponse.success(actualizado, "Producto actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminarProducto(@PathVariable Long id) {
        log.info("Eliminando producto ID: {}", id);
        productoService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado exitosamente"));
    }

    // ========================================================================
    // ENDPOINTS DE GESTIÓN DE INVENTARIO
    // ========================================================================

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("Actualizando stock del producto ID: {} a cantidad: {}", id, quantity);
        productoService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock actualizado exitosamente"));
    }

    @GetMapping("/stock-bajo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductoSummaryDTO>>> getProductosStockBajo() {
        List<ProductoSummaryDTO> productos = productoService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(productos, "Productos con stock bajo encontrados exitosamente"));
    }

    @GetMapping("/restock-necesario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductoSummaryDTO>>> getProductosRestockNecesario(
            @RequestParam(defaultValue = "10") int threshold) {
        List<ProductoSummaryDTO> productos = productoService.getProductsNeedingRestock(threshold);
        return ResponseEntity.ok(ApiResponse.success(productos, "Productos que necesitan restock encontrados exitosamente"));
    }
}
