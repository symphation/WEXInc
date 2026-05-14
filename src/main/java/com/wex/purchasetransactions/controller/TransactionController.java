package com.wex.purchasetransactions.controller;

import com.wex.purchasetransactions.dto.ConvertedTransactionResponse;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import com.wex.purchasetransactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Purchase Transactions", description = "Store and retrieve purchase transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // ── POST /api/transactions ─────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Store a new purchase transaction",
            description = """
                    Accepts a purchase transaction with a description, date, and USD amount.
                    The amount is automatically rounded to the nearest cent before being saved.
                    Returns the stored transaction along with its system-assigned unique ID.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction stored successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionResponse.class),
                            examples = @ExampleObject(ref = "#/components/examples/TransactionResponse_Created"))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — one or more fields failed validation",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(ref = "#/components/examples/ErrorResponse_Validation")))
    })
    public TransactionResponse createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The transaction details to store",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateTransactionRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Team lunch",
                                            ref = "#/components/examples/CreateTransactionRequest_Lunch"),
                                    @ExampleObject(
                                            name = "Software subscription",
                                            ref = "#/components/examples/CreateTransactionRequest_Software")
                            }))
            @Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    // ── GET /api/transactions/{id} ─────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a transaction with currency conversion",
            description = """
                    Fetches a stored transaction by ID and converts the original USD amount to the
                    requested currency using the U.S. Treasury Reporting Rates of Exchange.

                    **Conversion rules:**
                    - The rate used is the most recent one on or before the transaction date
                    - It must fall within 6 months before the transaction date
                    - If no valid rate is found, a 404 is returned

                    **Currency format:** use the Treasury API's `Country-Currency` style, e.g.:
                    `Canada-Dollar`, `Euro Zone-Euro`, `Japan-Yen`, `United Kingdom-Pound`
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved and converted successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConvertedTransactionResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Canadian Dollar",
                                            ref = "#/components/examples/ConvertedTransactionResponse_CAD"),
                                    @ExampleObject(
                                            name = "Euro",
                                            ref = "#/components/examples/ConvertedTransactionResponse_EUR")
                            })),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found **or** no Treasury exchange rate available within 6 months of the purchase date",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Transaction not found",
                                            ref = "#/components/examples/ErrorResponse_NotFound"),
                                    @ExampleObject(
                                            name = "Rate unavailable",
                                            ref = "#/components/examples/ErrorResponse_RateUnavailable")
                            })),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or malformed `currency` query parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(ref = "#/components/examples/ErrorResponse_Validation")))
    })
    public ConvertedTransactionResponse getTransactionWithConversion(
            @Parameter(
                    description = "The unique ID of the transaction (UUID format)",
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                    required = true)
            @PathVariable UUID id,

            @Parameter(
                    description = """
                            Target currency in Treasury API `Country-Currency` format.
                            Examples: `Canada-Dollar`, `Euro Zone-Euro`, `Japan-Yen`, `United Kingdom-Pound`
                            """,
                    example = "Canada-Dollar",
                    required = true)
            @RequestParam String currency) {
        return transactionService.getTransactionWithConversion(id, currency);
    }
}
