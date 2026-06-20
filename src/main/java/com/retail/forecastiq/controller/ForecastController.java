package com.retail.forecastiq.controller;

import com.retail.forecastiq.dto.ForecastRequestDto;
import com.retail.forecastiq.dto.ForecastResponseDto;
import com.retail.forecastiq.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
@Tag(name = "Forecasts", description = "Demand forecasting using SMA and WMA algorithms")
public class ForecastController {

    private final ForecastService forecastService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a demand forecast for a product",
               description = "Runs SMA or WMA over the configured window and returns a 7-day forecast plus inventory optimization metrics.")
    public ResponseEntity<ForecastResponseDto> generate(@Valid @RequestBody ForecastRequestDto request) {
        return ResponseEntity.ok(forecastService.generateForecast(request));
    }

    @GetMapping("/product/{productId}/history")
    @Operation(summary = "Retrieve previously generated forecasts for a product")
    public ResponseEntity<List<ForecastResponseDto>> getHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(forecastService.getHistoryForProduct(productId));
    }
}
