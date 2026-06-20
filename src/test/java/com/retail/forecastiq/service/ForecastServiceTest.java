package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.ForecastRequestDto;
import com.retail.forecastiq.dto.ForecastResponseDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import com.retail.forecastiq.enums.ForecastAlgorithm;
import com.retail.forecastiq.exception.InsufficientDataException;
import com.retail.forecastiq.exception.ResourceNotFoundException;
import com.retail.forecastiq.repository.ForecastResultRepository;
import com.retail.forecastiq.repository.SalesHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForecastService unit tests")
class ForecastServiceTest {

    @Mock private SalesHistoryRepository salesHistoryRepository;
    @Mock private ForecastResultRepository forecastResultRepository;
    @Mock private ProductService productService;
    @Mock private InventoryService inventoryService;

    @InjectMocks
    private ForecastService forecastService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .unitPrice(50.0)
                .currentStock(100)
                .leadTimeDays(5)
                .build();
    }

    // ------------------------------------------------------------------ SMA
    @Nested
    @DisplayName("Simple Moving Average (SMA)")
    class SmaTests {

        @Test
        @DisplayName("7-value window returns correct arithmetic mean")
        void computeSMA_sevenValues_returnsCorrectMean() {
            List<Double> values = Arrays.asList(10.0, 12.0, 11.0, 13.0, 14.0, 12.0, 11.0);
            double expected = (10 + 12 + 11 + 13 + 14 + 12 + 11) / 7.0;

            double result = forecastService.computeSMA(values);

            assertThat(result).isCloseTo(expected, within(0.001));
        }

        @Test
        @DisplayName("Empty list returns 0")
        void computeSMA_emptyList_returnsZero() {
            assertThat(forecastService.computeSMA(Collections.emptyList())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Null list returns 0")
        void computeSMA_nullList_returnsZero() {
            assertThat(forecastService.computeSMA(null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Single value returns that value")
        void computeSMA_singleValue_returnsThatValue() {
            assertThat(forecastService.computeSMA(Collections.singletonList(42.0))).isEqualTo(42.0);
        }

        @Test
        @DisplayName("All identical values return that same value")
        void computeSMA_allIdentical_returnsThatValue() {
            List<Double> values = Collections.nCopies(7, 10.0);
            assertThat(forecastService.computeSMA(values)).isCloseTo(10.0, within(0.001));
        }

        @Test
        @DisplayName("SMA with large window including zeros")
        void computeSMA_withZeroValues_handlesCorrectly() {
            List<Double> values = Arrays.asList(0.0, 0.0, 10.0, 20.0);
            assertThat(forecastService.computeSMA(values)).isCloseTo(7.5, within(0.001));
        }
    }

    // ------------------------------------------------------------------ WMA
    @Nested
    @DisplayName("Weighted Moving Average (WMA)")
    class WmaTests {

        @Test
        @DisplayName("WMA of 3 values matches manual weighted calculation")
        void computeWMA_threeValues_matchesManualCalculation() {
            // values[0]=7 (most recent), values[1]=5, values[2]=3 (oldest)
            // weights: 3, 2, 1 → WMA = (3×7 + 2×5 + 1×3) / 6 = 34/6
            List<Double> values = Arrays.asList(7.0, 5.0, 3.0);

            double result = forecastService.computeWMA(values);

            assertThat(result).isCloseTo(34.0 / 6.0, within(0.001));
        }

        @Test
        @DisplayName("WMA gives higher result than SMA when most-recent value is highest")
        void computeWMA_recentHighValue_exceedsSma() {
            // Most recent (index 0) is significantly higher than the rest
            List<Double> values = Arrays.asList(20.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);

            double sma = forecastService.computeSMA(values);
            double wma = forecastService.computeWMA(values);

            assertThat(wma).isGreaterThan(sma);
        }

        @Test
        @DisplayName("WMA gives lower result than SMA when most-recent value is lowest")
        void computeWMA_recentLowValue_belowSma() {
            // Most recent (index 0) is significantly lower
            List<Double> values = Arrays.asList(2.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);

            double sma = forecastService.computeSMA(values);
            double wma = forecastService.computeWMA(values);

            assertThat(wma).isLessThan(sma);
        }

        @Test
        @DisplayName("Empty list returns 0")
        void computeWMA_emptyList_returnsZero() {
            assertThat(forecastService.computeWMA(Collections.emptyList())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Null list returns 0")
        void computeWMA_nullList_returnsZero() {
            assertThat(forecastService.computeWMA(null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Single value returns that value")
        void computeWMA_singleValue_returnsThatValue() {
            assertThat(forecastService.computeWMA(Collections.singletonList(15.0))).isCloseTo(15.0, within(0.001));
        }

        @Test
        @DisplayName("All identical values return that same value (SMA == WMA)")
        void computeWMA_allIdentical_equalsValue() {
            List<Double> values = Collections.nCopies(7, 10.0);
            assertThat(forecastService.computeWMA(values)).isCloseTo(10.0, within(0.001));
        }

        @Test
        @DisplayName("WMA with strictly increasing values (oldest→newest ascending) returns value above midpoint")
        void computeWMA_increasingValues_resultAboveSma() {
            // values stored most-recent first: [7, 6, 5, 4, 3, 2, 1]
            List<Double> values = Arrays.asList(7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
            double sma = forecastService.computeSMA(values); // = 4.0

            double wma = forecastService.computeWMA(values);

            assertThat(wma).isGreaterThan(sma);
        }
    }

    // ------------------------------------------------------------------ StdDev
    @Nested
    @DisplayName("Standard Deviation")
    class StdDevTests {

        @Test
        @DisplayName("Known population stddev: {2,4,4,4,5,5,7,9} → stddev = 2")
        void computeStdDev_knownValues_returnsCorrectResult() {
            List<Double> values = Arrays.asList(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);
            // mean=5, variance=4, stddev=2
            assertThat(forecastService.computeStdDev(values)).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("Single value returns 0")
        void computeStdDev_singleValue_returnsZero() {
            assertThat(forecastService.computeStdDev(Collections.singletonList(10.0))).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Null/empty list returns 0")
        void computeStdDev_emptyOrNull_returnsZero() {
            assertThat(forecastService.computeStdDev(null)).isEqualTo(0.0);
            assertThat(forecastService.computeStdDev(Collections.emptyList())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("All identical values have stddev = 0")
        void computeStdDev_identicalValues_returnsZero() {
            List<Double> values = Collections.nCopies(5, 7.0);
            assertThat(forecastService.computeStdDev(values)).isCloseTo(0.0, within(0.001));
        }
    }

    // ------------------------------------------------------------------ generateForecast
    @Nested
    @DisplayName("generateForecast — integration with mocks")
    class GenerateForecastTests {

        @Test
        @DisplayName("Throws InsufficientDataException when fewer than 2 sales records exist")
        void generateForecast_onlyOneRecord_throwsInsufficientDataException() {
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct))
                    .thenReturn(Collections.singletonList(buildSale(5)));

            ForecastRequestDto request = buildRequest(ForecastAlgorithm.SMA);

            assertThatThrownBy(() -> forecastService.generateForecast(request))
                    .isInstanceOf(InsufficientDataException.class)
                    .hasMessageContaining("Insufficient sales data");
        }

        @Test
        @DisplayName("Throws InsufficientDataException when sales list is empty")
        void generateForecast_noData_throwsInsufficientDataException() {
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct))
                    .thenReturn(Collections.emptyList());

            ForecastRequestDto request = buildRequest(ForecastAlgorithm.SMA);

            assertThatThrownBy(() -> forecastService.generateForecast(request))
                    .isInstanceOf(InsufficientDataException.class);
        }

        @Test
        @DisplayName("SMA forecast returns correct daily demand for 7-day window")
        void generateForecast_sma_returnsCorrectDemand() {
            List<SalesHistory> history = buildSalesList(10, 11, 12, 13, 14, 12, 11);
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct)).thenReturn(history);
            when(forecastResultRepository.saveAll(any())).thenReturn(Collections.emptyList());
            when(inventoryService.computeSafetyStock(anyDouble(), anyInt(), anyDouble())).thenReturn(8.0);
            when(inventoryService.computeReorderPoint(anyDouble(), anyInt(), anyDouble())).thenReturn(68.0);
            when(inventoryService.computeEOQ(anyDouble(), anyDouble(), anyDouble())).thenReturn(200.0);

            ForecastRequestDto request = buildRequest(ForecastAlgorithm.SMA);
            ForecastResponseDto response = forecastService.generateForecast(request);

            double expectedSma = (10 + 11 + 12 + 13 + 14 + 12 + 11) / 7.0;
            assertThat(response.getForecastedDailyDemand()).isCloseTo(expectedSma, within(0.01));
            assertThat(response.getAlgorithm()).isEqualTo(ForecastAlgorithm.SMA);
            assertThat(response.getNext7DaysForecast()).hasSize(7);
        }

        @Test
        @DisplayName("WMA produces higher demand than SMA when most-recent values are highest")
        void generateForecast_wma_returnsDemandBiasedToRecent() {
            // Most recent values (index 0,1) are much higher than the rest → WMA > SMA
            List<Double> demands = List.of(20.0, 20.0, 10.0, 10.0, 10.0, 10.0, 10.0);

            double smaResult = forecastService.computeSMA(demands);
            double wmaResult = forecastService.computeWMA(demands);

            assertThat(wmaResult).isGreaterThan(smaResult);
        }

        @Test
        @DisplayName("needsReorder flag is true when stock <= reorderPoint")
        void generateForecast_stockAtReorderPoint_needsReorderIsTrue() {
            sampleProduct.setCurrentStock(50);
            List<SalesHistory> history = buildSalesList(10, 10, 10, 10, 10, 10, 10);
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct)).thenReturn(history);
            when(forecastResultRepository.saveAll(any())).thenReturn(Collections.emptyList());
            when(inventoryService.computeSafetyStock(anyDouble(), anyInt(), anyDouble())).thenReturn(8.22);
            // Reorder point > current stock to trigger flag
            when(inventoryService.computeReorderPoint(anyDouble(), anyInt(), anyDouble())).thenReturn(58.22);
            when(inventoryService.computeEOQ(anyDouble(), anyDouble(), anyDouble())).thenReturn(200.0);

            ForecastResponseDto response = forecastService.generateForecast(buildRequest(ForecastAlgorithm.SMA));

            assertThat(response.getNeedsReorder()).isTrue();
        }

        @Test
        @DisplayName("Response contains 7-day daily breakdown")
        void generateForecast_returnsSevenDayBreakdown() {
            List<SalesHistory> history = buildSalesList(12, 14, 11, 13, 12, 15, 10);
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct)).thenReturn(history);
            when(forecastResultRepository.saveAll(any())).thenReturn(Collections.emptyList());
            when(inventoryService.computeSafetyStock(anyDouble(), anyInt(), anyDouble())).thenReturn(5.0);
            when(inventoryService.computeReorderPoint(anyDouble(), anyInt(), anyDouble())).thenReturn(55.0);
            when(inventoryService.computeEOQ(anyDouble(), anyDouble(), anyDouble())).thenReturn(200.0);

            ForecastResponseDto response = forecastService.generateForecast(buildRequest(ForecastAlgorithm.SMA));

            assertThat(response.getNext7DaysForecast()).hasSize(7);
            assertThat(response.getNext7DaysForecast().get(0).getDate())
                    .isEqualTo(LocalDate.now().plusDays(1));
        }

        @Test
        @DisplayName("dataPointsUsed reflects actual window size applied")
        void generateForecast_dataPointsUsedReflectsWindow() {
            List<SalesHistory> history = buildSalesList(10, 12, 11, 13, 14, 12, 11, 15, 9, 13);
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductOrderBySaleDateDesc(sampleProduct)).thenReturn(history);
            when(forecastResultRepository.saveAll(any())).thenReturn(Collections.emptyList());
            when(inventoryService.computeSafetyStock(anyDouble(), anyInt(), anyDouble())).thenReturn(5.0);
            when(inventoryService.computeReorderPoint(anyDouble(), anyInt(), anyDouble())).thenReturn(55.0);
            when(inventoryService.computeEOQ(anyDouble(), anyDouble(), anyDouble())).thenReturn(200.0);

            ForecastRequestDto request = buildRequest(ForecastAlgorithm.SMA);
            request.setWindowSize(7);
            ForecastResponseDto response = forecastService.generateForecast(request);

            assertThat(response.getDataPointsUsed()).isEqualTo(7);
        }
    }

    // ------------------------------------------------------------------ Helpers
    private ForecastRequestDto buildRequest(ForecastAlgorithm algorithm) {
        return ForecastRequestDto.builder()
                .productId(1L)
                .algorithm(algorithm)
                .windowSize(7)
                .serviceLevel(0.95)
                .orderingCost(50.0)
                .holdingCostPerUnit(10.0)
                .build();
    }

    private SalesHistory buildSale(int qty) {
        return SalesHistory.builder()
                .product(sampleProduct)
                .saleDate(LocalDate.now().minusDays(1))
                .quantitySold(qty)
                .revenue(qty * 50.0)
                .build();
    }

    private List<SalesHistory> buildSalesList(int... quantities) {
        return IntStream.range(0, quantities.length)
                .mapToObj(i -> SalesHistory.builder()
                        .product(sampleProduct)
                        .saleDate(LocalDate.now().minusDays(i + 1))
                        .quantitySold(quantities[i])
                        .revenue(quantities[i] * 50.0)
                        .build())
                .toList();
    }
}
