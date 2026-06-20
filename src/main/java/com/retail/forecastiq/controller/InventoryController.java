package com.retail.forecastiq.controller;

import com.retail.forecastiq.dto.InventoryOptimizationRequestDto;
import com.retail.forecastiq.dto.InventoryOptimizationResponseDto;
import com.retail.forecastiq.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory optimization: safety stock, reorder point, EOQ")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/optimize")
    @Operation(summary = "Compute inventory optimization metrics for a product",
               description = "Calculates Safety Stock, Reorder Point, and Economic Order Quantity " +
                             "based on historical demand and the provided cost parameters.")
    public ResponseEntity<InventoryOptimizationResponseDto> optimize(
            @Valid @RequestBody InventoryOptimizationRequestDto request) {
        return ResponseEntity.ok(inventoryService.optimize(request));
    }
}
