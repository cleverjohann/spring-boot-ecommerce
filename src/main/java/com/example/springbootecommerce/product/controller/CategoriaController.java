package com.example.springbootecommerce.product.controller;

import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.dto.CreateCategoriaDTO;
import com.example.springbootecommerce.product.service.CategoriaService;
import com.example.springbootecommerce.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.springbootecommerce.shared.util.Constants.CATEGORIES_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(CATEGORIES_ENDPOINT)
@RequiredArgsConstructor
@Validated
@Tag(name = "Categorias", description = "API de gestion de categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    // ========================================================================
    // ENDPOINTS PÚBLICOS
    // ========================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaDTO>>> getAllCategories(){
        log.debug("REST request to get all Categories");

        List<CategoriaDTO> categoriaDTOS = categoriaService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categoriaDTOS, "Categorias raiz encontradas"));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<CategoriaDTO>>> getRootCategories(){
        log.debug("REST request to get root Categories");

        List<CategoriaDTO> categoriaDTOS = categoriaService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(categoriaDTOS,"Categorias raiz encontradas"));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoriaTreeDTO>>> getRootCategoriesTree(){
        log.debug("REST request to get root Categories tree");

        List<CategoriaTreeDTO> categoriaTree = categoriaService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(categoriaTree,"Árbol de categorías obtenida"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaDTO>> getCategoriaById(@PathVariable Long id){
        log.debug("REST request to get Categoria by id {}", id);

        CategoriaDTO categoria = categoriaService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(categoria, "Categoria encontrada encontradas"));
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<ApiResponse<List<CategoriaDTO>>> getSubcategorias(@PathVariable Long id){
        log.debug("REST request to get subcategorias {}", id);

        List<CategoriaDTO> subcategorias = categoriaService.getSubcategories(id);
        return ResponseEntity.ok(ApiResponse.success(subcategorias,"Subcategorias encontradas"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoriaDTO>>> getSearchedCategories(@RequestParam String searchTerm){
        log.debug("REST request to get searched Categories {}", searchTerm);

        List<CategoriaDTO> categorias = categoriaService.searchCategories(searchTerm);
        return ResponseEntity.ok(ApiResponse.success(categorias,"Categorias encontradas"));
    }

    // ========================================================================
    // ENDPOINTS ADMINISTRATIVOS
    // ========================================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaDTO>> createCategoria(@Valid @RequestBody CreateCategoriaDTO createCategoriaDTO){
        log.info("REST request to create Categoria : {}", createCategoriaDTO);

        CategoriaDTO createCategoria = categoriaService.createCategory(createCategoriaDTO);
        return ResponseEntity.ok(ApiResponse.success(createCategoria,"Categoria encontrada encontradas"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaDTO>> updateCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO categoriaDTO){
        log.info("REST request to update Categoria : {}", categoriaDTO);

        CategoriaDTO updateCategoria = categoriaService.updateCategory(id, categoriaDTO);
        return ResponseEntity.ok(ApiResponse.success(updateCategoria,"Categoria encontrada encontradas"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoriaDTO>> deleteCategoria(@PathVariable Long id){
        log.info("REST request to delete Categoria : {}", id);
        categoriaService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Categoria eliminada exitosamente"));
    }
}
