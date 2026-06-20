package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.DailyForecastDto;
import com.retail.forecastiq.dto.ForecastRequestDto;
import com.retail.forecastiq.dto.ForecastResponseDto;
import com.retail.forecastiq.entity.ForecastResult;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import com.retail.forecastiq.enums.ForecastAlgorithm;
import com.retail.forecastiq.exception.InsufficientDataException;
import com.retail.forecastiq.repository.ForecastResultRepository;
import com.retail.forecastiq.repository.SalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForecastService {

    private static final int DEFAULT_WINDOW_SIZE = 7;
    private static final int MIN_DATA_POINTS = 2;
    private static final double DEFAULT_SERVICE_LEVEL = 0.95;
    private static final double DEFAULT_ORDERING_COST = 50.0;

    private final SalesHistoryRepository salesHistoryRepository;
    private final ForecastResultRepository forecastResultRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Transactional
    public ForecastResponseDto generateForecast(ForecastRequestDto request) {
        Product product = productService.findById(request.getProductId());

        List<SalesHistory> history = salesHistoryRepository.findByProductOrderBySaleDateDesc(product);

        if (history.size() < MIN_DATA_POINTS) {
            throw new InsufficientDataException(
                    "Insufficient sales data for product '" + product.getSku() + "'. " +
                    "Need at least " + MIN_DATA_POINTS + " records, found " + history.size() + ".");
        }

        int windowSize = request.getWindowSize() != null ? request.getWindowSize() : DEFAULT_WINDOW_SIZE;
        List<Double> demandWindow = history.stream()
                .limit(windowSize)
                .map(sh -> sh.getQuantitySold().doubleValue())
                .collect(Collectors.toList());

        double forecastedDailyDemand = switch (request.getAlgorithm()) {
            case SMA -> computeSMA(demandWindow);
            case WMA -> computeWMA(demandWindow);
        };

        double serviceLevel = request.getServiceLevel() != null ? request.getServiceLevel() : DEFAULT_SERVICE_LEVEL;
        double orderingCost = request.getOrderingCost() != null ? request.getOrderingCost() : DEFAULT_ORDERING_COST;
        double holdingCost = request.getHoldingCostPerUnit() != null
                ? request.getHoldingCostPerUnit()
                : (product.getUnitPrice() != null ? product.getUnitPrice() * 0.20 : 10.0);

        double stdDev = computeStdDev(demandWindow);
        int leadTime = product.getLeadTimeDays() != null ? product.getLeadTimeDays() : 7;

        double safetyStock = inventoryService.computeSafetyStock(stdDev, leadTime, serviceLevel);
        double reorderPoint = inventoryService.computeReorderPoint(forecastedDailyDemand, leadTime, safetyStock);
        double eoq = inventoryService.computeEOQ(forecastedDailyDemand, orderingCost, holdingCost);

        List<DailyForecastDto> next7Days = buildNext7DaysForecast(forecastedDailyDemand);
        persistForecastResults(product, forecastedDailyDemand, request.getAlgorithm(), next7Days);

        int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        boolean needsReorder = currentStock <= reorderPoint;

        log.info("Forecast generated for product={} algo={} demand={} rop={} needsReorder={}",
                product.getSku(), request.getAlgorithm(), round(forecastedDailyDemand), round(reorderPoint), needsReorder);

        return ForecastResponseDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .algorithm(request.getAlgorithm())
                .forecastedDailyDemand(round(forecastedDailyDemand))
                .next7DaysForecast(next7Days)
                .safetyStock(round(safetyStock))
                .reorderPoint(round(reorderPoint))
                .economicOrderQuantity(round(eoq))
                .currentStock(currentStock)
                .needsReorder(needsReorder)
                .serviceLevel(serviceLevel)
                .dataPointsUsed(demandWindow.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public List<ForecastResponseDto> getHistoryForProduct(Long productId) {
        Product product = productService.findById(productId);
        return forecastResultRepository.findByProductOrderByGeneratedAtDesc(product)
                .stream()
                .map(fr -> ForecastResponseDto.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .productSku(product.getSku())
                        .algorithm(fr.getAlgorithm())
                        .forecastedDailyDemand(fr.getForecastedDemand())
                        .generatedAt(fr.getGeneratedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Simple Moving Average: equal weight across the window.
     * values[0] is most recent; values[n-1] is oldest.
     */
    public double computeSMA(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Weighted Moving Average: linearly increasing weights so the most recent
     * observation (values[0]) receives the highest weight.
     * Weight for values[i] = (n - i), giving weights n, n-1, ..., 1.
     */
    public double computeWMA(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        int n = values.size();
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        for (int i = 0; i < n; i++) {
            double weight = n - i;
            weightedSum += weight * values.get(i);
            totalWeight += weight;
        }
        return weightedSum / totalWeight;
    }

    /**
     * Population standard deviation of the demand window.
     */
    public double computeStdDev(List<Double> values) {
        if (values == null || values.size() < 2) return 0.0;
        double mean = computeSMA(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private List<DailyForecastDto> buildNext7DaysForecast(double dailyDemand) {
        List<DailyForecastDto> forecast = new ArrayList<>();
        LocalDate start = LocalDate.now().plusDays(1);
        for (int i = 0; i < 7; i++) {
            forecast.add(DailyForecastDto.builder()
                    .date(start.plusDays(i))
                    .forecastedDemand(round(dailyDemand))
                    .build());
        }
        return forecast;
    }

    private void persistForecastResults(Product product, double demand, ForecastAlgorithm algorithm,
                                         List<DailyForecastDto> next7Days) {
        List<ForecastResult> results = next7Days.stream()
                .map(df -> ForecastResult.builder()
                        .product(product)
                        .forecastDate(df.getDate())
                        .forecastedDemand(demand)
                        .algorithm(algorithm)
                        .build())
                .collect(Collectors.toList());
        forecastResultRepository.saveAll(results);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
