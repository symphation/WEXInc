package com.wex.purchasetransactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new purchase transaction")
public class CreateTransactionRequest {

    @Schema(
            description = "A short description of the purchase (max 50 characters)",
            example = "Team lunch at downtown bistro",
            maxLength = 50)
    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @Schema(
            description = "The date the purchase was made (ISO 8601 format: YYYY-MM-DD)",
            example = "2025-06-01",
            type = "string",
            format = "date")
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @Schema(
            description = "The purchase amount in US dollars. Must be positive; will be rounded to the nearest cent.",
            example = "87.50",
            minimum = "0.01")
    @NotNull(message = "Purchase amount is required")
    @Positive(message = "Purchase amount must be a positive value")
    @Digits(integer = 10, fraction = 10, message = "Purchase amount must be a valid numeric value")
    private BigDecimal purchaseAmount;
}
