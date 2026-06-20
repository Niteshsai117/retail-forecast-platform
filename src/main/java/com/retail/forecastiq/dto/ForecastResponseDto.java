package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retail.forecastiq.enums.ForecastAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Demand forecast result with inventory optimization metrics")
public class ForecastResponseDto {

    @Schema(description = "Product ID")
    private Long productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Product SKU")
    private String productSku;

    @Schema(description = "Algorithm used for forecasting")
    private ForecastAlgorithm algorithm;

    @Schema(description = "Forecasted average daily demand (units/day)", example = "13.4")
    private Double forecastedDailyDemand;

    @Schema(description = "7-day rolling forecast broken down by date")
    private List<DailyForecastDto> next7DaysForecast;

    @Schema(description = "Safety stock level (units)", example = "24.6")
    private Double safetyStock;

    @Schema(description = "Reorder point — trigger a new order when stock reaches this level", example = "91.6")
    private Double reorderPoint;

    @Schema(description = "Economic Order Quantity — optimal order size to minimize total inventory cost",
            example = "245.0")
    private Double economicOrderQuantity;

    @Schema(description = "Current stock on hand")
    private Integer currentStock;

    @Schema(description = "True when current stock is at or below the reorder point")
    private Boolean needsReorder;

    @Schema(description = "Service level used for safety stock calculation", example = "0.95")
    private Double serviceLevel;

    @Schema(description = "Number of historical data points used in the window", example = "7")
    private Integer dataPointsUsed;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the forecast was generated")
    private LocalDateTime generatedAt;
}
