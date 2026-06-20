package com.retail.forecastiq.dto;

import com.retail.forecastiq.enums.ForecastAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for demand forecast generation")
public class ForecastRequestDto {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to forecast", example = "1")
    private Long productId;

    @NotNull(message = "Forecast algorithm is required")
    @Schema(description = "Algorithm to use: SMA (Simple Moving Average) or WMA (Weighted Moving Average)",
            example = "SMA")
    private ForecastAlgorithm algorithm;

    @Min(value = 2, message = "Window size must be at least 2")
    @Schema(description = "Number of historical days to use as the moving window (default: 7)", example = "7")
    private Integer windowSize;

    @DecimalMin(value = "0.80", message = "Service level must be at least 0.80")
    @DecimalMax(value = "0.99", message = "Service level must be at most 0.99")
    @Schema(description = "Desired service level for safety stock calculation (default: 0.95)", example = "0.95")
    private Double serviceLevel;

    @Min(value = 0, message = "Ordering cost must be non-negative")
    @Schema(description = "Fixed cost per order for EOQ calculation (default: 50.0)", example = "50.0")
    private Double orderingCost;

    @Min(value = 0, message = "Holding cost must be non-negative")
    @Schema(description = "Annual holding cost per unit for EOQ calculation (default: 20% of unit price)",
            example = "15.0")
    private Double holdingCostPerUnit;
}
