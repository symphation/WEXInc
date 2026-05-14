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
@Schema(description = "Confirmation of a stored purchase transaction")
public class TransactionResponse {

    @Schema(description = "System-assigned unique identifier for this transaction", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID id;

    @Schema(description = "Description of the purchase", example = "Team lunch at downtown bistro")
    private String description;

    @Schema(description = "Date the purchase was made", example = "2025-06-01")
    private LocalDate transactionDate;

    @Schema(description = "Purchase amount in US dollars, rounded to the nearest cent", example = "87.50")
    private BigDecimal purchaseAmount;
}
