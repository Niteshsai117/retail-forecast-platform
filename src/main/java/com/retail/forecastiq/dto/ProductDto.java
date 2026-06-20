package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product data transfer object")
public class ProductDto {

    @Schema(description = "Product ID (auto-generated)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Schema(description = "Product display name", example = "Wireless Headphones")
    private String name;

    @NotBlank(message = "SKU is required")
    @Schema(description = "Unique stock-keeping unit identifier", example = "ELEC-001")
    private String sku;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Min(value = 0, message = "Unit price must be non-negative")
    @Schema(description = "Price per unit in USD", example = "79.99")
    private Double unitPrice;

    @Min(value = 0, message = "Current stock must be non-negative")
    @Schema(description = "Current inventory count", example = "150")
    private Integer currentStock;

    @Min(value = 1, message = "Lead time must be at least 1 day")
    @Schema(description = "Supplier lead time in days", example = "5")
    private Integer leadTimeDays;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
