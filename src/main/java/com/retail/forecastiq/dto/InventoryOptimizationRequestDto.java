package com.retail.forecastiq.dto;

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
@Schema(description = "Request payload for standalone inventory optimization")
public class InventoryOptimizationRequestDto {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to optimize", example = "1")
    private Long productId;

    @Min(value = 0, message = "Ordering cost must be non-negative")
    @Schema(description = "Fixed cost per purchase order (default: 50.0)", example = "50.0")
    private Double orderingCost;

    @Min(value = 0, message = "Holding cost must be non-negative")
    @Schema(description = "Annual holding cost per unit (default: 20% of unit price)", example = "16.0")
    private Double holdingCostPerUnit;

    @DecimalMin(value = "0.80", message = "Service level must be at least 0.80")
    @DecimalMax(value = "0.99", message = "Service level must be at most 0.99")
    @Schema(description = "Target service level for safety stock (default: 0.95)", example = "0.95")
    private Double serviceLevel;
}
