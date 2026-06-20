package com.retail.forecastiq.controller;

import com.retail.forecastiq.dto.SalesHistoryDto;
import com.retail.forecastiq.service.SalesHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales History", description = "Record and retrieve historical sales data")
public class SalesHistoryController {

    private final SalesHistoryService salesHistoryService;

    @PostMapping
    @Operation(summary = "Record a single sale")
    public ResponseEntity<SalesHistoryDto> recordSale(@Valid @RequestBody SalesHistoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesHistoryService.recordSale(dto));
    }

    @PostMapping("/batch")
    @Operation(summary = "Record multiple sales in one request")
    public ResponseEntity<List<SalesHistoryDto>> recordBatch(@Valid @RequestBody List<SalesHistoryDto> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesHistoryService.recordBatchSales(dtos));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all sales for a product (most recent first)")
    public ResponseEntity<List<SalesHistoryDto>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(salesHistoryService.getSalesForProduct(productId));
    }

    @GetMapping("/product/{productId}/range")
    @Operation(summary = "Get sales for a product within a date range")
    public ResponseEntity<List<SalesHistoryDto>> getByProductAndDateRange(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(salesHistoryService.getSalesInDateRange(productId, from, to));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sales record")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        salesHistoryService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }
}
