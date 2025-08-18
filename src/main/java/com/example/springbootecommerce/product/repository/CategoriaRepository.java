package com.example.springbootecommerce.product.repository;

import com.example.springbootecommerce.product.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria,Long> {

    // Encontrar por nombre (para evitar duplicados)
    Categoria findByName(String name);

    // Encontrar categories rail (sin parent)
    @Query("SELECT c FROM Categoria c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Categoria> findRootCategories();

    // Encontrar categories con sus productos (JOIN FETCH para evitar N+1)
    @Query("SELECT DISTINCT c FROM Categoria c LEFT JOIN FETCH c.productos p WHERE c.isActive = true AND (p.isActive = true OR p IS NULL)")
    List<Categoria> findCategoriesWithActiveProducts();

    // Contar productos activos por categoria
    @Query("SELECT c.id, COUNT(p) FROM Categoria c LEFT JOIN c.productos p WHERE c.isActive = true AND (p.isActive = true OR p IS NULL) GROUP BY c.id")
    List<Object[]> countActiveProductsByCategory();

    // Búsqueda por jerarquía completa
    @Query("SELECT c FROM Categoria c WHERE c.isActive = true AND (c.name ILIKE %:searchTerm% OR c.description ILIKE %:searchTerm%)")
    List<Categoria> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

}
