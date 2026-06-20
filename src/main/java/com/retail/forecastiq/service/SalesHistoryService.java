package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.SalesHistoryDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import com.retail.forecastiq.exception.ResourceNotFoundException;
import com.retail.forecastiq.repository.SalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesHistoryService {

    private final SalesHistoryRepository salesHistoryRepository;
    private final ProductService productService;

    @Transactional
    public SalesHistoryDto recordSale(SalesHistoryDto dto) {
        Product product = productService.findById(dto.getProductId());
        double revenue = dto.getRevenue() != null
                ? dto.getRevenue()
                : (product.getUnitPrice() != null ? product.getUnitPrice() * dto.getQuantitySold() : 0.0);

        SalesHistory sale = SalesHistory.builder()
                .product(product)
                .saleDate(dto.getSaleDate())
                .quantitySold(dto.getQuantitySold())
                .revenue(revenue)
                .build();

        SalesHistory saved = salesHistoryRepository.save(sale);
        log.info("Recorded sale for product={} date={} qty={}", product.getSku(), dto.getSaleDate(), dto.getQuantitySold());
        return toDto(saved);
    }

    @Transactional
    public List<SalesHistoryDto> recordBatchSales(List<SalesHistoryDto> dtos) {
        return dtos.stream().map(this::recordSale).collect(Collectors.toList());
    }

    public List<SalesHistoryDto> getSalesForProduct(Long productId) {
        Product product = productService.findById(productId);
        return salesHistoryRepository.findByProductOrderBySaleDateDesc(product)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<SalesHistoryDto> getSalesInDateRange(Long productId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before or equal to 'to' date");
        }
        Product product = productService.findById(productId);
        return salesHistoryRepository
                .findByProductAndSaleDateBetweenOrderBySaleDateDesc(product, from, to)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSale(Long id) {
        SalesHistory sale = salesHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesHistory", id));
        salesHistoryRepository.delete(sale);
        log.info("Deleted sales record id={}", id);
    }

    private SalesHistoryDto toDto(SalesHistory sh) {
        return SalesHistoryDto.builder()
                .id(sh.getId())
                .productId(sh.getProduct().getId())
                .productName(sh.getProduct().getName())
                .productSku(sh.getProduct().getSku())
                .saleDate(sh.getSaleDate())
                .quantitySold(sh.getQuantitySold())
                .revenue(sh.getRevenue())
                .build();
    }
}
