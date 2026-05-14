package com.wex.purchasetransactions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    // Using BigDecimal for monetary values to avoid floating-point rounding issues
    @Column(name = "purchase_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchaseAmount;
}
