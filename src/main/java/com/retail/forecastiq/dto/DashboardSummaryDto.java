package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Platform-wide summary for the operations dashboard")
public class DashboardSummaryDto {

    @Schema(description = "Total number of products in the catalog")
    private Long totalProducts;

    @Schema(description = "Number of individual sales records in the last 30 days")
    private Long totalSalesRecordsLast30Days;

    @Schema(description = "Total revenue generated in the last 30 days")
    private Double totalRevenueLast30Days;

    @Schema(description = "Total units sold across all products in the last 30 days")
    private Long totalUnitsSoldLast30Days;

    @Schema(description = "Number of products currently at or below their reorder point")
    private Long productsNeedingReorder;

    @Schema(description = "Detailed reorder alerts for action")
    private List<ProductReorderAlertDto> reorderAlerts;

    @Schema(description = "Average daily demand per product category")
    private Map<String, Double> avgDailyDemandByCategory;

    @Schema(description = "Top 5 products by sales volume in the last 30 days")
    private List<ProductSalesSummaryDto> topSellingProducts;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "When this summary was generated")
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesSummaryDto {
        private Long productId;
        private String productName;
        private String sku;
        private String category;
        private Long totalUnitsSold;
        private Double totalRevenue;
    }
}
