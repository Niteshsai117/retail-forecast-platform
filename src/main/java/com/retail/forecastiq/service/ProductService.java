package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.ProductDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.exception.ResourceNotFoundException;
import com.retail.forecastiq.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + dto.getSku() + "' already exists");
        }
        Product saved = productRepository.save(toEntity(dto));
        log.info("Created product: {} ({})", saved.getName(), saved.getSku());
        return toDto(saved);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        return toDto(findById(id));
    }

    public ProductDto getProductBySku(String sku) {
        return toDto(productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU", sku)));
    }

    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product existing = findById(id);
        if (!existing.getSku().equals(dto.getSku()) && productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + dto.getSku() + "' already exists");
        }
        existing.setName(dto.getName());
        existing.setSku(dto.getSku());
        existing.setCategory(dto.getCategory());
        existing.setUnitPrice(dto.getUnitPrice());
        existing.setCurrentStock(dto.getCurrentStock());
        existing.setLeadTimeDays(dto.getLeadTimeDays());
        log.info("Updated product id={}", id);
        return toDto(productRepository.save(existing));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
        log.info("Deleted product id={}", id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .category(p.getCategory())
                .unitPrice(p.getUnitPrice())
                .currentStock(p.getCurrentStock())
                .leadTimeDays(p.getLeadTimeDays())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private Product toEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .category(dto.getCategory())
                .unitPrice(dto.getUnitPrice())
                .currentStock(dto.getCurrentStock() != null ? dto.getCurrentStock() : 0)
                .leadTimeDays(dto.getLeadTimeDays() != null ? dto.getLeadTimeDays() : 7)
                .build();
    }
}
