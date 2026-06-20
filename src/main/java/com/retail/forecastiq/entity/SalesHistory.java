package com.retail.forecastiq.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sales_history", indexes = {
        @Index(name = "idx_sales_product_date", columnList = "product_id, sale_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "product")
@ToString(exclude = "product")
public class SalesHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private LocalDate saleDate;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantitySold;

    private Double revenue;
}
