package com.retail.forecastiq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alert for a product that has reached or fallen below its reorder point")
public class ProductReorderAlertDto {

    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private Integer currentStock;
    private Double reorderPoint;
    private Double suggestedOrderQuantity;

    @Schema(description = "Urgency level: CRITICAL, HIGH, MEDIUM")
    private String urgency;
}
