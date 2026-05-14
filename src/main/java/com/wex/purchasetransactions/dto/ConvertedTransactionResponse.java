package com.wex.purchasetransactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A stored transaction with the purchase amount converted to the requested currency")
public class ConvertedTransactionResponse {

    @Schema(description = "Unique identifier of the transaction", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID id;

    @Schema(description = "Description of the purchase", example = "Team lunch at downtown bistro")
    private String description;

    @Schema(description = "Date the purchase was made", example = "2025-06-01")
    private LocalDate transactionDate;

    @Schema(description = "Original purchase amount in US dollars", example = "87.50")
    private BigDecimal originalPurchaseAmount;

    @Schema(description = "Treasury exchange rate used for conversion (units of target currency per 1 USD)", example = "1.435")
    private BigDecimal exchangeRate;

    @Schema(description = "Purchase amount converted to the target currency, rounded to the nearest cent", example = "125.56")
    private BigDecimal convertedAmount;

    @Schema(description = "Target currency in Treasury API Country-Currency format", example = "Canada-Dollar")
    private String targetCurrency;
}
