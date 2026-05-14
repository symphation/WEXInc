package com.wex.purchasetransactions.dto;

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
public class CreateTransactionRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Purchase amount is required")
    @Positive(message = "Purchase amount must be a positive value")
    @Digits(integer = 10, fraction = 2, message = "Purchase amount must be a valid dollar amount (up to 2 decimal places)")
    private BigDecimal purchaseAmount;
}
