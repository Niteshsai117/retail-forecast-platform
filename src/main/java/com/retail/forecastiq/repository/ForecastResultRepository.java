package com.retail.forecastiq.repository;

import com.retail.forecastiq.entity.ForecastResult;
import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.enums.ForecastAlgorithm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastResultRepository extends JpaRepository<ForecastResult, Long> {

    List<ForecastResult> findByProductOrderByGeneratedAtDesc(Product product);

    List<ForecastResult> findByProductAndAlgorithmOrderByGeneratedAtDesc(
            Product product, ForecastAlgorithm algorithm);

    Optional<ForecastResult> findTopByProductOrderByGeneratedAtDesc(Product product);

    List<ForecastResult> findByGeneratedAtAfter(LocalDateTime dateTime);

    void deleteByProductAndGeneratedAtBefore(Product product, LocalDateTime before);
}
