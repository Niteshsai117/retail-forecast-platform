package com.retail.forecastiq.entity;

import com.retail.forecastiq.enums.ForecastAlgorithm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "forecast_results", indexes = {
        @Index(name = "idx_forecast_product_date", columnList = "product_id, forecast_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "product")
@ToString(exclude = "product")
public class ForecastResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDate forecastDate;

    @Column(nullable = false)
    private Double forecastedDemand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastAlgorithm algorithm;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedAt;
}
