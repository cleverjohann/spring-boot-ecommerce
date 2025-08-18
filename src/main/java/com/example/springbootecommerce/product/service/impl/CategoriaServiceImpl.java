package com.example.springbootecommerce.product.service.impl;

import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.service.CategoriaService;

import java.util.List;

public class CategoriaServiceImpl implements CategoriaService {
    @Override
    public List<CategoriaDTO> getAllCategories() {
        return List.of();
    }

    @Override
    public List<CategoriaDTO> getRootCategories() {
        return List.of();
    }

    @Override
    public List<CategoriaTreeDTO> getCategoryTree() {
        return List.of();
    }

    @Override
    public CategoriaDTO getCategoryById(Long id) {
        return null;
    }

    @Override
    public List<CategoriaDTO> getSubcategories(Long parentId) {
        return List.of();
    }

    @Override
    public List<CategoriaDTO> searchCategories(String searchTerm) {
        return List.of();
    }

    @Override
    public CategoriaDTO createCategory(CategoriaDTO createCategoriaDTO) {
        return null;
    }

    @Override
    public CategoriaDTO updateCategory(Long id, CategoriaDTO updateCategoriaDTO) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {

    }
}
