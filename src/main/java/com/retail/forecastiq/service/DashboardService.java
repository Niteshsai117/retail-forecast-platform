package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.DashboardSummaryDto;
import com.retail.forecastiq.dto.DashboardSummaryDto.ProductSalesSummaryDto;
import com.retail.forecastiq.dto.ProductReorderAlertDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import com.retail.forecastiq.repository.ProductRepository;
import com.retail.forecastiq.repository.SalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final SalesHistoryRepository salesHistoryRepository;
    private final InventoryService inventoryService;

    public DashboardSummaryDto getSummary() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        long totalProducts = productRepository.count();
        List<SalesHistory> recentSales = salesHistoryRepository.findBySaleDateAfter(thirtyDaysAgo);

        double totalRevenue = recentSales.stream()
                .mapToDouble(sh -> sh.getRevenue() != null ? sh.getRevenue() : 0.0)
                .sum();

        long totalUnitsSold = recentSales.stream()
                .mapToLong(SalesHistory::getQuantitySold)
                .sum();

        List<Product> allProducts = productRepository.findAll();
        List<ProductReorderAlertDto> reorderAlerts = buildReorderAlerts(allProducts, thirtyDaysAgo);

        Map<String, Double> avgDemandByCategory = buildAvgDemandByCategory(thirtyDaysAgo);
        List<ProductSalesSummaryDto> topSellers = buildTopSellers(recentSales, allProducts);

        return DashboardSummaryDto.builder()
                .totalProducts(totalProducts)
                .totalSalesRecordsLast30Days((long) recentSales.size())
                .totalRevenueLast30Days(round(totalRevenue))
                .totalUnitsSoldLast30Days(totalUnitsSold)
                .productsNeedingReorder((long) reorderAlerts.size())
                .reorderAlerts(reorderAlerts)
                .avgDailyDemandByCategory(avgDemandByCategory)
                .topSellingProducts(topSellers)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private List<ProductReorderAlertDto> buildReorderAlerts(List<Product> products, LocalDate since) {
        List<ProductReorderAlertDto> alerts = new ArrayList<>();
        for (Product product : products) {
            if (product.getCurrentStock() == null || product.getLeadTimeDays() == null) continue;

            List<SalesHistory> history = salesHistoryRepository.findByProductAndSaleDateAfter(product, since);
            if (history.isEmpty()) continue;

            double avgDemand = history.stream()
                    .mapToInt(SalesHistory::getQuantitySold)
                    .average()
                    .orElse(0.0);
            double stdDev = inventoryService.computeStdDev(
                    history.stream().map(sh -> sh.getQuantitySold().doubleValue()).toList());

            double safetyStock = inventoryService.computeSafetyStock(stdDev, product.getLeadTimeDays(), 0.95);
            double reorderPoint = inventoryService.computeReorderPoint(avgDemand, product.getLeadTimeDays(), safetyStock);
            double eoq = inventoryService.computeEOQ(avgDemand, 50.0,
                    product.getUnitPrice() != null ? product.getUnitPrice() * 0.20 : 10.0);

            if (product.getCurrentStock() <= reorderPoint) {
                String urgency = product.getCurrentStock() == 0 ? "CRITICAL"
                        : product.getCurrentStock() < safetyStock ? "HIGH" : "MEDIUM";

                alerts.add(ProductReorderAlertDto.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .sku(product.getSku())
                        .category(product.getCategory())
                        .currentStock(product.getCurrentStock())
                        .reorderPoint(round(reorderPoint))
                        .suggestedOrderQuantity(round(eoq))
                        .urgency(urgency)
                        .build());
            }
        }
        alerts.sort(Comparator.comparing(a -> urgencyOrder(a.getUrgency())));
        return alerts;
    }

    private Map<String, Double> buildAvgDemandByCategory(LocalDate since) {
        List<Object[]> rows = salesHistoryRepository.findAvgDailyDemandByCategorySince(since);
        Map<String, Double> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], round((Double) row[1]));
        }
        return result;
    }

    private List<ProductSalesSummaryDto> buildTopSellers(List<SalesHistory> sales, List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return sales.stream()
                .collect(Collectors.groupingBy(sh -> sh.getProduct().getId()))
                .entrySet().stream()
                .map(e -> {
                    Long productId = e.getKey();
                    List<SalesHistory> productSales = e.getValue();
                    Product p = productMap.get(productId);
                    long totalUnits = productSales.stream().mapToLong(SalesHistory::getQuantitySold).sum();
                    double totalRevenue = productSales.stream()
                            .mapToDouble(sh -> sh.getRevenue() != null ? sh.getRevenue() : 0.0).sum();
                    return ProductSalesSummaryDto.builder()
                            .productId(productId)
                            .productName(p != null ? p.getName() : "Unknown")
                            .sku(p != null ? p.getSku() : "N/A")
                            .category(p != null ? p.getCategory() : null)
                            .totalUnitsSold(totalUnits)
                            .totalRevenue(round(totalRevenue))
                            .build();
                })
                .sorted(Comparator.comparingLong(ProductSalesSummaryDto::getTotalUnitsSold).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private int urgencyOrder(String urgency) {
        return switch (urgency) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            default -> 2;
        };
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
