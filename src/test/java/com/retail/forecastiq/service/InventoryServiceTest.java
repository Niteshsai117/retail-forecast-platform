package com.retail.forecastiq.service;

import com.retail.forecastiq.dto.InventoryOptimizationRequestDto;
import com.retail.forecastiq.dto.InventoryOptimizationResponseDto;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService unit tests")
class InventoryServiceTest {

    @Mock private SalesHistoryRepository salesHistoryRepository;
    @Mock private ProductService productService;

    @InjectMocks
    private InventoryService inventoryService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .unitPrice(80.0)
                .currentStock(100)
                .leadTimeDays(5)
                .build();
    }

    // ------------------------------------------------------------------ Safety Stock
    @Nested
    @DisplayName("Safety Stock = Z × σ × √(leadTime)")
    class SafetyStockTests {

        @Test
        @DisplayName("95% service level: Z=1.645, σ=5, leadTime=4 → 1.645×5×2 = 16.45")
        void computeSafetyStock_95ServiceLevel_returnsCorrectValue() {
            double result = inventoryService.computeSafetyStock(5.0, 4, 0.95);
            assertThat(result).isCloseTo(16.45, within(0.01));
        }

        @Test
        @DisplayName("99% service level: Z=2.326, σ=3, leadTime=9 → 2.326×3×3 = 20.934")
        void computeSafetyStock_99ServiceLevel_returnsCorrectValue() {
            double result = inventoryService.computeSafetyStock(3.0, 9, 0.99);
            assertThat(result).isCloseTo(20.934, within(0.01));
        }

        @Test
        @DisplayName("90% service level: Z=1.282, σ=4, leadTime=16 → 1.282×4×4 = 20.512")
        void computeSafetyStock_90ServiceLevel_returnsCorrectValue() {
            double result = inventoryService.computeSafetyStock(4.0, 16, 0.90);
            assertThat(result).isCloseTo(20.512, within(0.01));
        }

        @Test
        @DisplayName("Zero standard deviation → safety stock = 0")
        void computeSafetyStock_zeroStdDev_returnsZero() {
            assertThat(inventoryService.computeSafetyStock(0.0, 7, 0.95)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Zero lead time → safety stock = 0")
        void computeSafetyStock_zeroLeadTime_returnsZero() {
            assertThat(inventoryService.computeSafetyStock(5.0, 0, 0.95)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Both zero → returns 0 without exception")
        void computeSafetyStock_bothZero_returnsZero() {
            assertThat(inventoryService.computeSafetyStock(0.0, 0, 0.95)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Higher service level produces higher safety stock")
        void computeSafetyStock_higherServiceLevel_producesHigherStock() {
            double ss90 = inventoryService.computeSafetyStock(5.0, 4, 0.90);
            double ss95 = inventoryService.computeSafetyStock(5.0, 4, 0.95);
            double ss99 = inventoryService.computeSafetyStock(5.0, 4, 0.99);

            assertThat(ss90).isLessThan(ss95);
            assertThat(ss95).isLessThan(ss99);
        }
    }

    // ------------------------------------------------------------------ Reorder Point
    @Nested
    @DisplayName("Reorder Point = (avgDemand × leadTime) + safetyStock")
    class ReorderPointTests {

        @Test
        @DisplayName("Typical case: avgDemand=10, leadTime=5, safetyStock=20 → 70")
        void computeReorderPoint_typicalValues_returnsCorrectROP() {
            double result = inventoryService.computeReorderPoint(10.0, 5, 20.0);
            assertThat(result).isCloseTo(70.0, within(0.001));
        }

        @Test
        @DisplayName("Zero lead time → ROP equals safety stock only")
        void computeReorderPoint_zeroLeadTime_returnsOnlySafetyStock() {
            double result = inventoryService.computeReorderPoint(10.0, 0, 15.0);
            assertThat(result).isCloseTo(15.0, within(0.001));
        }

        @Test
        @DisplayName("Zero demand and zero safety stock → ROP = 0")
        void computeReorderPoint_allZeros_returnsZero() {
            assertThat(inventoryService.computeReorderPoint(0.0, 7, 0.0)).isCloseTo(0.0, within(0.001));
        }

        @Test
        @DisplayName("ROP increases linearly with lead time")
        void computeReorderPoint_longerLeadTime_producesHigherROP() {
            double rop5 = inventoryService.computeReorderPoint(10.0, 5, 10.0);
            double rop10 = inventoryService.computeReorderPoint(10.0, 10, 10.0);
            assertThat(rop10).isGreaterThan(rop5);
            assertThat(rop10 - rop5).isCloseTo(50.0, within(0.001));
        }
    }

    // ------------------------------------------------------------------ EOQ
    @Nested
    @DisplayName("Economic Order Quantity = √(2DS/H)")
    class EoqTests {

        @Test
        @DisplayName("EOQ: avgDemand=10/day, orderCost=50, holdCost=2 → √(2×3650×50/2)")
        void computeEOQ_typicalValues_returnsCorrectEOQ() {
            double annualDemand = 10.0 * 365;
            double expected = Math.sqrt(2 * annualDemand * 50.0 / 2.0);

            double result = inventoryService.computeEOQ(10.0, 50.0, 2.0);

            assertThat(result).isCloseTo(expected, within(0.01));
        }

        @Test
        @DisplayName("EOQ: avgDemand=20/day, orderCost=100, holdCost=5 → correct result")
        void computeEOQ_higherDemandAndCosts_returnsCorrectEOQ() {
            double annualDemand = 20.0 * 365;
            double expected = Math.sqrt(2 * annualDemand * 100.0 / 5.0);

            double result = inventoryService.computeEOQ(20.0, 100.0, 5.0);

            assertThat(result).isCloseTo(expected, within(0.01));
        }

        @Test
        @DisplayName("Zero holding cost → returns MAX_VALUE (cannot hold, order unlimited)")
        void computeEOQ_zeroHoldingCost_returnsMaxValue() {
            double result = inventoryService.computeEOQ(10.0, 50.0, 0.0);
            assertThat(result).isEqualTo(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("Zero daily demand → EOQ = 0")
        void computeEOQ_zeroDemand_returnsZero() {
            double result = inventoryService.computeEOQ(0.0, 50.0, 2.0);
            assertThat(result).isCloseTo(0.0, within(0.001));
        }

        @Test
        @DisplayName("Higher ordering cost → larger EOQ (order more per trip)")
        void computeEOQ_higherOrderingCost_producesLargerEOQ() {
            double eoqLow = inventoryService.computeEOQ(10.0, 10.0, 2.0);
            double eoqHigh = inventoryService.computeEOQ(10.0, 100.0, 2.0);
            assertThat(eoqHigh).isGreaterThan(eoqLow);
        }

        @Test
        @DisplayName("Higher holding cost → smaller EOQ (order less per trip)")
        void computeEOQ_higherHoldingCost_producesSmallerEOQ() {
            double eoqLow = inventoryService.computeEOQ(10.0, 50.0, 1.0);
            double eoqHigh = inventoryService.computeEOQ(10.0, 50.0, 10.0);
            assertThat(eoqHigh).isLessThan(eoqLow);
        }
    }

    // ------------------------------------------------------------------ Z-score
    @Nested
    @DisplayName("Z-score lookup")
    class ZScoreTests {

        @Test
        @DisplayName("99% → 2.326")
        void getZScore_99_returns2326() {
            assertThat(inventoryService.getZScore(0.99)).isCloseTo(2.326, within(0.001));
        }

        @Test
        @DisplayName("95% → 1.645")
        void getZScore_95_returns1645() {
            assertThat(inventoryService.getZScore(0.95)).isCloseTo(1.645, within(0.001));
        }

        @Test
        @DisplayName("90% → 1.282")
        void getZScore_90_returns1282() {
            assertThat(inventoryService.getZScore(0.90)).isCloseTo(1.282, within(0.001));
        }

        @Test
        @DisplayName("Below 90% → 1.0 (84.1% service level)")
        void getZScore_below90_returns1() {
            assertThat(inventoryService.getZScore(0.84)).isCloseTo(1.0, within(0.001));
        }
    }

    // ------------------------------------------------------------------ Standard Deviation
    @Nested
    @DisplayName("Standard Deviation")
    class StdDevTests {

        @Test
        @DisplayName("Known population stddev: {2,4,4,4,5,5,7,9} → 2.0")
        void computeStdDev_knownValues_returns2() {
            List<Double> values = Arrays.asList(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0);
            assertThat(inventoryService.computeStdDev(values)).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("All identical values → stddev = 0")
        void computeStdDev_identicalValues_returnsZero() {
            List<Double> values = Collections.nCopies(6, 10.0);
            assertThat(inventoryService.computeStdDev(values)).isCloseTo(0.0, within(0.001));
        }

        @Test
        @DisplayName("Single value → 0")
        void computeStdDev_singleValue_returnsZero() {
            assertThat(inventoryService.computeStdDev(Collections.singletonList(5.0))).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Null list → 0")
        void computeStdDev_nullList_returnsZero() {
            assertThat(inventoryService.computeStdDev(null)).isEqualTo(0.0);
        }
    }

    // ------------------------------------------------------------------ optimize (integration)
    @Nested
    @DisplayName("optimize() — full workflow")
    class OptimizeTests {

        @Test
        @DisplayName("Returns needsReorder=true when stock is below computed reorder point")
        void optimize_stockBelowReorderPoint_needsReorderTrue() {
            sampleProduct.setCurrentStock(20); // very low stock
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductAndSaleDateAfter(eq(sampleProduct), any(LocalDate.class)))
                    .thenReturn(buildSalesList(10, 10, 10, 10, 10, 10, 10));

            InventoryOptimizationRequestDto request = InventoryOptimizationRequestDto.builder()
                    .productId(1L)
                    .serviceLevel(0.95)
                    .orderingCost(50.0)
                    .holdingCostPerUnit(16.0)
                    .build();

            InventoryOptimizationResponseDto response = inventoryService.optimize(request);

            assertThat(response.getNeedsReorder()).isTrue();
            assertThat(response.getReorderPoint()).isGreaterThan(response.getCurrentStock().doubleValue());
        }

        @Test
        @DisplayName("Response contains non-empty recommendations")
        void optimize_alwaysReturnsRecommendations() {
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductAndSaleDateAfter(eq(sampleProduct), any(LocalDate.class)))
                    .thenReturn(buildSalesList(12, 14, 11, 13, 12));

            InventoryOptimizationRequestDto request = InventoryOptimizationRequestDto.builder()
                    .productId(1L)
                    .build();

            InventoryOptimizationResponseDto response = inventoryService.optimize(request);

            assertThat(response.getRecommendations()).isNotEmpty();
        }

        @Test
        @DisplayName("Empty sales history → avgDailyDemand = 0, no exception")
        void optimize_noSalesHistory_handlesGracefully() {
            when(productService.findById(1L)).thenReturn(sampleProduct);
            when(salesHistoryRepository.findByProductAndSaleDateAfter(eq(sampleProduct), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            InventoryOptimizationRequestDto request = InventoryOptimizationRequestDto.builder()
                    .productId(1L)
                    .build();

            InventoryOptimizationResponseDto response = inventoryService.optimize(request);

            assertThat(response.getAvgDailyDemand()).isEqualTo(0.0);
            assertThat(response.getSafetyStock()).isEqualTo(0.0);
        }
    }

    // ------------------------------------------------------------------ Helpers
    private List<SalesHistory> buildSalesList(int... quantities) {
        return java.util.stream.IntStream.range(0, quantities.length)
                .mapToObj(i -> SalesHistory.builder()
                        .product(sampleProduct)
                        .saleDate(LocalDate.now().minusDays(i + 1))
                        .quantitySold(quantities[i])
                        .revenue(quantities[i] * 80.0)
                        .build())
                .toList();
    }
}
