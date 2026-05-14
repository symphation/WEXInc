package com.wex.purchasetransactions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertedTransactionResponse {

    private UUID id;
    private String description;
    private LocalDate transactionDate;
    private BigDecimal originalPurchaseAmount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String targetCurrency;
}
