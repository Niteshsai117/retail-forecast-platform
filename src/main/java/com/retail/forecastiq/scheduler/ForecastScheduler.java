package com.retail.forecastiq.scheduler;

import com.retail.forecastiq.dto.ForecastRequestDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.enums.ForecastAlgorithm;
import com.retail.forecastiq.exception.InsufficientDataException;
import com.retail.forecastiq.repository.ProductRepository;
import com.retail.forecastiq.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ForecastScheduler {

    private final ProductRepository productRepository;
    private final ForecastService forecastService;

    /**
     * Runs every day at 01:00 AM.
     * Generates SMA-7 forecasts for all products that have sufficient sales history.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyForecast() {
        log.info("=== Daily Forecast Job started at {} ===", LocalDateTime.now());

        List<Product> products = productRepository.findAll();
        int success = 0;
        int skipped = 0;
        int failed = 0;

        for (Product product : products) {
            try {
                ForecastRequestDto request = ForecastRequestDto.builder()
                        .productId(product.getId())
                        .algorithm(ForecastAlgorithm.SMA)
                        .windowSize(7)
                        .serviceLevel(0.95)
                        .orderingCost(50.0)
                        .holdingCostPerUnit(product.getUnitPrice() != null
                                ? product.getUnitPrice() * 0.20 : 10.0)
                        .build();

                forecastService.generateForecast(request);
                success++;
                log.debug("Forecast generated for product: {}", product.getSku());

            } catch (InsufficientDataException e) {
                skipped++;
                log.warn("Skipped product {} — insufficient data: {}", product.getSku(), e.getMessage());
            } catch (Exception e) {
                failed++;
                log.error("Failed to forecast product {}: {}", product.getSku(), e.getMessage(), e);
            }
        }

        log.info("=== Daily Forecast Job complete — success={}, skipped={}, failed={} ===",
                success, skipped, failed);
    }

    /**
     * Runs every Sunday at 02:00 AM to also generate WMA forecasts,
     * providing an alternative signal for the weekly review.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void runWeeklyWmaForecast() {
        log.info("=== Weekly WMA Forecast Job started at {} ===", LocalDateTime.now());

        List<Product> products = productRepository.findAll();
        int success = 0;

        for (Product product : products) {
            try {
                ForecastRequestDto request = ForecastRequestDto.builder()
                        .productId(product.getId())
                        .algorithm(ForecastAlgorithm.WMA)
                        .windowSize(14)
                        .serviceLevel(0.95)
                        .orderingCost(50.0)
                        .holdingCostPerUnit(product.getUnitPrice() != null
                                ? product.getUnitPrice() * 0.20 : 10.0)
                        .build();

                forecastService.generateForecast(request);
                success++;
            } catch (InsufficientDataException e) {
                log.warn("WMA skipped for {}: {}", product.getSku(), e.getMessage());
            } catch (Exception e) {
                log.error("WMA failed for {}: {}", product.getSku(), e.getMessage());
            }
        }

        log.info("=== Weekly WMA Forecast complete — {} products processed ===", success);
    }
}
