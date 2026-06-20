package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inventory optimization result")
public class InventoryOptimizationResponseDto {

    private Long productId;
    private String productName;
    private String productSku;

    @Schema(description = "Average daily demand computed from last 30 days of sales", example = "12.8")
    private Double avgDailyDemand;

    @Schema(description = "Standard deviation of daily demand", example = "3.2")
    private Double demandStdDev;

    @Schema(description = "Supplier lead time in days")
    private Integer leadTimeDays;

    @Schema(description = "Calculated safety stock level", example = "20.5")
    private Double safetyStock;

    @Schema(description = "Reorder point — order when stock falls to this level", example = "84.5")
    private Double reorderPoint;

    @Schema(description = "Economic Order Quantity", example = "312.0")
    private Double economicOrderQuantity;

    @Schema(description = "Current units on hand")
    private Integer currentStock;

    @Schema(description = "Units below reorder point (negative means surplus)", example = "-65.5")
    private Double stockVsReorderPoint;

    @Schema(description = "True if immediate reorder action is needed")
    private Boolean needsReorder;

    @Schema(description = "Service level used for this calculation")
    private Double serviceLevel;

    @Schema(description = "Actionable recommendations")
    private List<String> recommendations;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;
}
