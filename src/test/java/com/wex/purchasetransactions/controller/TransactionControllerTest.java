package com.wex.purchasetransactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.purchasetransactions.dto.ConvertedTransactionResponse;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import com.wex.purchasetransactions.exception.ExchangeRateNotFoundException;
import com.wex.purchasetransactions.exception.ExternalApiException;
import com.wex.purchasetransactions.exception.GlobalExceptionHandler;
import com.wex.purchasetransactions.exception.TransactionNotFoundException;
import com.wex.purchasetransactions.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Nested
    @DisplayName("POST /api/transactions")
    class CreateEndpoint {

        @Test
        @DisplayName("should return 201 when the request is valid")
        void validRequest() throws Exception {
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .description("Office supplies")
                    .transactionDate(LocalDate.of(2024, 12, 15))
                    .purchaseAmount(new BigDecimal("150.42"))
                    .build();

            UUID generatedId = UUID.randomUUID();
            TransactionResponse response = TransactionResponse.builder()
                    .id(generatedId)
                    .description("Office supplies")
                    .transactionDate(LocalDate.of(2024, 12, 15))
                    .purchaseAmount(new BigDecimal("150.42"))
                    .build();

            when(transactionService.createTransaction(any())).thenReturn(response);

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(generatedId.toString()))
                    .andExpect(jsonPath("$.description").value("Office supplies"))
                    .andExpect(jsonPath("$.purchaseAmount").value(150.42));
        }

        @Test
        @DisplayName("should return 400 when description exceeds 50 characters")
        void descriptionTooLong() throws Exception {
            String longDescription = "A".repeat(51);
            String body = """
                    {
                        "description": "%s",
                        "transactionDate": "2024-12-15",
                        "purchaseAmount": 100.00
                    }
                    """.formatted(longDescription);

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.description").exists());
        }

        @Test
        @DisplayName("should return 400 when description is blank")
        void blankDescription() throws Exception {
            String body = """
                    {
                        "description": "",
                        "transactionDate": "2024-12-15",
                        "purchaseAmount": 100.00
                    }
                    """;

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.description").exists());
        }

        @Test
        @DisplayName("should return 400 when purchase amount is negative")
        void negativeAmount() throws Exception {
            String body = """
                    {
                        "description": "Refund",
                        "transactionDate": "2024-12-15",
                        "purchaseAmount": -50.00
                    }
                    """;

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.purchaseAmount").exists());
        }

        @Test
        @DisplayName("should return 400 when purchase amount is zero")
        void zeroAmount() throws Exception {
            String body = """
                    {
                        "description": "Free item",
                        "transactionDate": "2024-12-15",
                        "purchaseAmount": 0
                    }
                    """;

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.purchaseAmount").exists());
        }

        @Test
        @DisplayName("should return 400 when transaction date is missing")
        void missingDate() throws Exception {
            String body = """
                    {
                        "description": "Something",
                        "purchaseAmount": 100.00
                    }
                    """;

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.transactionDate").exists());
        }

        @Test
        @DisplayName("should return 400 when JSON is malformed")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("not json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Malformed Request"));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class GetEndpoint {

        @Test
        @DisplayName("should return 200 with the converted transaction")
        void validConversion() throws Exception {
            UUID id = UUID.randomUUID();
            ConvertedTransactionResponse response = ConvertedTransactionResponse.builder()
                    .id(id)
                    .description("Travel booking")
                    .transactionDate(LocalDate.of(2025, 1, 10))
                    .originalPurchaseAmount(new BigDecimal("200.00"))
                    .exchangeRate(new BigDecimal("1.362"))
                    .convertedAmount(new BigDecimal("272.40"))
                    .targetCurrency("Canada-Dollar")
                    .build();

            when(transactionService.getTransactionWithConversion(eq(id), eq("Canada-Dollar")))
                    .thenReturn(response);

            mockMvc.perform(get("/api/transactions/{id}", id)
                            .param("currency", "Canada-Dollar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.originalPurchaseAmount").value(200.00))
                    .andExpect(jsonPath("$.exchangeRate").value(1.362))
                    .andExpect(jsonPath("$.convertedAmount").value(272.40))
                    .andExpect(jsonPath("$.targetCurrency").value("Canada-Dollar"));
        }

        @Test
        @DisplayName("should return 404 when the transaction doesn't exist")
        void notFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(transactionService.getTransactionWithConversion(eq(id), any()))
                    .thenThrow(new TransactionNotFoundException(id));

            mockMvc.perform(get("/api/transactions/{id}", id)
                            .param("currency", "Canada-Dollar"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("should return 422 when no exchange rate is available")
        void noExchangeRate() throws Exception {
            UUID id = UUID.randomUUID();
            when(transactionService.getTransactionWithConversion(eq(id), eq("Narnia-GoldCoin")))
                    .thenThrow(new ExchangeRateNotFoundException("Narnia-GoldCoin", "2025-01-10"));

            mockMvc.perform(get("/api/transactions/{id}", id)
                            .param("currency", "Narnia-GoldCoin"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("cannot be converted")));
        }

        @Test
        @DisplayName("should return 502 when the Treasury API is unavailable")
        void treasuryApiDown() throws Exception {
            UUID id = UUID.randomUUID();
            when(transactionService.getTransactionWithConversion(eq(id), any()))
                    .thenThrow(new ExternalApiException("Connection refused"));

            mockMvc.perform(get("/api/transactions/{id}", id)
                            .param("currency", "Canada-Dollar"))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.error").value("External Service Error"));
        }

        @Test
        @DisplayName("should return 400 when the currency parameter is missing")
        void missingCurrency() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(get("/api/transactions/{id}", id))
                    .andExpect(status().isBadRequest());
        }
    }
}
