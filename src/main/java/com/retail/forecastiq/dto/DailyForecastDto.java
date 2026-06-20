package com.retail.forecastiq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single-day forecast entry")
public class DailyForecastDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Forecast date", example = "2026-06-21")
    private LocalDate date;

    @Schema(description = "Forecasted units demand for this day", example = "13.4")
    private Double forecastedDemand;
}
