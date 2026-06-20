package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.InventoryOptimizationRequestDto;
import com.retail.forecastiq.dto.InventoryOptimizationResponseDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import com.retail.forecastiq.repository.SalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private static final double DEFAULT_SERVICE_LEVEL = 0.95;
    private static final double DEFAULT_ORDERING_COST = 50.0;
    private static final int DEMAND_LOOKBACK_DAYS = 30;

    private final SalesHistoryRepository salesHistoryRepository;
    private final ProductService productService;

    public InventoryOptimizationResponseDto optimize(InventoryOptimizationRequestDto request) {
        Product product = productService.findById(request.getProductId());

        LocalDate from = LocalDate.now().minusDays(DEMAND_LOOKBACK_DAYS);
        List<SalesHistory> history = salesHistoryRepository.findByProductAndSaleDateAfter(product, from);

        List<Double> dailyDemands = history.stream()
                .map(sh -> sh.getQuantitySold().doubleValue())
                .toList();

        double avgDailyDemand = dailyDemands.isEmpty() ? 0.0
                : dailyDemands.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double stdDev = computeStdDev(dailyDemands);

        int leadTime = product.getLeadTimeDays() != null ? product.getLeadTimeDays() : 7;
        double serviceLevel = request.getServiceLevel() != null ? request.getServiceLevel() : DEFAULT_SERVICE_LEVEL;
        double orderingCost = request.getOrderingCost() != null ? request.getOrderingCost() : DEFAULT_ORDERING_COST;
        double holdingCost = request.getHoldingCostPerUnit() != null
                ? request.getHoldingCostPerUnit()
                : (product.getUnitPrice() != null ? product.getUnitPrice() * 0.20 : 10.0);

        double safetyStock = computeSafetyStock(stdDev, leadTime, serviceLevel);
        double reorderPoint = computeReorderPoint(avgDailyDemand, leadTime, safetyStock);
        double eoq = computeEOQ(avgDailyDemand, orderingCost, holdingCost);

        int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        boolean needsReorder = currentStock <= reorderPoint;

        List<String> recommendations = buildRecommendations(
                product, currentStock, reorderPoint, eoq, avgDailyDemand, leadTime);

        return InventoryOptimizationResponseDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .avgDailyDemand(round(avgDailyDemand))
                .demandStdDev(round(stdDev))
                .leadTimeDays(leadTime)
                .safetyStock(round(safetyStock))
                .reorderPoint(round(reorderPoint))
                .economicOrderQuantity(round(eoq))
                .currentStock(currentStock)
                .stockVsReorderPoint(round(currentStock - reorderPoint))
                .needsReorder(needsReorder)
                .serviceLevel(serviceLevel)
                .recommendations(recommendations)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Safety Stock = Z * σ * √(lead time)
     * Buffers against demand variability during the replenishment lead time.
     */
    public double computeSafetyStock(double demandStdDev, int leadTimeDays, double serviceLevel) {
        if (demandStdDev <= 0 || leadTimeDays <= 0) return 0.0;
        return getZScore(serviceLevel) * demandStdDev * Math.sqrt(leadTimeDays);
    }

    /**
     * Reorder Point = (Average Daily Demand × Lead Time) + Safety Stock
     */
    public double computeReorderPoint(double avgDailyDemand, int leadTimeDays, double safetyStock) {
        return (avgDailyDemand * leadTimeDays) + safetyStock;
    }

    /**
     * Economic Order Quantity = √(2DS/H)
     * D = annual demand, S = ordering cost per order, H = holding cost per unit per year
     */
    public double computeEOQ(double avgDailyDemand, double orderingCost, double holdingCostPerUnit) {
        if (holdingCostPerUnit <= 0) return Double.MAX_VALUE;
        double annualDemand = avgDailyDemand * 365;
        return Math.sqrt((2 * annualDemand * orderingCost) / holdingCostPerUnit);
    }

    public double computeStdDev(List<Double> values) {
        if (values == null || values.size() < 2) return 0.0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    public double getZScore(double serviceLevel) {
        if (serviceLevel >= 0.99) return 2.326;
        if (serviceLevel >= 0.98) return 2.054;
        if (serviceLevel >= 0.97) return 1.881;
        if (serviceLevel >= 0.96) return 1.751;
        if (serviceLevel >= 0.95) return 1.645;
        if (serviceLevel >= 0.90) return 1.282;
        return 1.0;
    }

    private List<String> buildRecommendations(Product product, int currentStock, double reorderPoint,
                                               double eoq, double avgDailyDemand, int leadTime) {
        List<String> recs = new ArrayList<>();
        if (currentStock <= reorderPoint) {
            recs.add(String.format("URGENT: Place order immediately. Current stock (%d) is at or below reorder point (%.1f).",
                    currentStock, reorderPoint));
            recs.add(String.format("Recommended order quantity: %.0f units (EOQ).", eoq));
        } else {
            double daysUntilReorder = avgDailyDemand > 0
                    ? (currentStock - reorderPoint) / avgDailyDemand : Double.MAX_VALUE;
            if (daysUntilReorder < leadTime * 2) {
                recs.add(String.format("Plan order within %.0f days to avoid stockout.", daysUntilReorder));
            } else {
                recs.add(String.format("Stock is healthy. Next reorder in approx. %.0f days.", daysUntilReorder));
            }
        }
        return recs;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
