package com.wex.purchasetransactions.controller;

import com.wex.purchasetransactions.dto.ConvertedTransactionResponse;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import com.wex.purchasetransactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Purchase Transactions", description = "Store and retrieve purchase transactions with currency conversion")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Store a new purchase transaction",
            description = "Persists a purchase transaction and assigns it a unique identifier")
    public TransactionResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a transaction converted to a target currency",
            description = "Fetches a stored transaction and converts the purchase amount using "
                    + "Treasury Reporting Rates of Exchange. The currency parameter should match "
                    + "the country-currency format from the Treasury API (e.g., 'Canada-Dollar').")
    public ConvertedTransactionResponse getTransactionWithConversion(
            @PathVariable UUID id,
            @Parameter(description = "Target currency in Treasury API format, e.g. 'Canada-Dollar', 'Japan-Yen'",
                    example = "Canada-Dollar", required = true)
            @RequestParam String currency) {
        return transactionService.getTransactionWithConversion(id, currency);
    }
}
