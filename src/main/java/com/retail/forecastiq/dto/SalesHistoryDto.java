package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Sales history record")
public class SalesHistoryDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product sold", example = "1")
    private Long productId;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String productName;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String productSku;

    @NotNull(message = "Sale date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the sale", example = "2026-06-01")
    private LocalDate saleDate;

    @NotNull(message = "Quantity sold is required")
    @Min(value = 0, message = "Quantity sold must be non-negative")
    @Schema(description = "Number of units sold", example = "12")
    private Integer quantitySold;

    @Schema(description = "Total revenue for this sale record", example = "959.88")
    private Double revenue;
}
