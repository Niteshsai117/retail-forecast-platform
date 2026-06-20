package com.retail.forecastiq.repository;

import com.retail.forecastiq.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryIgnoreCase(String category);

    boolean existsBySku(String sku);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();

    @Query("SELECT p FROM Product p WHERE p.currentStock <= :threshold")
    List<Product> findProductsWithLowStock(int threshold);
}
