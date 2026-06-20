package com.retail.forecastiq.repository;

import com.retail.forecastiq.entity.Product;
import com.retail.forecastiq.entity.SalesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {

    List<SalesHistory> findByProductOrderBySaleDateDesc(Product product);

    List<SalesHistory> findByProductAndSaleDateBetweenOrderBySaleDateDesc(
            Product product, LocalDate from, LocalDate to);

    List<SalesHistory> findBySaleDateAfter(LocalDate date);

    List<SalesHistory> findByProductAndSaleDateAfter(Product product, LocalDate date);

    boolean existsByProductAndSaleDate(Product product, LocalDate saleDate);

    @Query("SELECT SUM(sh.quantitySold) FROM SalesHistory sh WHERE sh.product = :product AND sh.saleDate >= :from")
    Long sumQuantitySoldByProductSince(@Param("product") Product product, @Param("from") LocalDate from);

    @Query("SELECT sh.product.id, SUM(sh.quantitySold) as totalQty " +
           "FROM SalesHistory sh WHERE sh.saleDate >= :from " +
           "GROUP BY sh.product.id ORDER BY totalQty DESC")
    List<Object[]> findTopProductsBySalesVolumeSince(@Param("from") LocalDate from);

    @Query("SELECT sh.product.category, AVG(sh.quantitySold) " +
           "FROM SalesHistory sh WHERE sh.saleDate >= :from AND sh.product.category IS NOT NULL " +
           "GROUP BY sh.product.category")
    List<Object[]> findAvgDailyDemandByCategorySince(@Param("from") LocalDate from);
}
