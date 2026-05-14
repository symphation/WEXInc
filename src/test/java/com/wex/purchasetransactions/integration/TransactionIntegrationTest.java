package com.wex.purchasetransactions.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static MockWebServer treasuryApi;

    @BeforeAll
    static void startMockServer() throws Exception {
        treasuryApi = new MockWebServer();
        treasuryApi.start();
    }

    @AfterAll
    static void stopMockServer() throws Exception {
        treasuryApi.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("treasury.api.base-url", () -> treasuryApi.url("/").toString());
    }

    @Test
    @DisplayName("full flow: store a transaction, then retrieve it with currency conversion")
    void endToEndFlow() throws Exception {
        // Step 1: Store a transaction
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .description("End-to-end test purchase")
                .transactionDate(LocalDate.of(2025, 1, 15))
                .purchaseAmount(new BigDecimal("250.00"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("End-to-end test purchase"))
                .andReturn();

        TransactionResponse stored = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TransactionResponse.class);

        // Step 2: Queue up a Treasury API response for the conversion
        String treasuryResponse = """
                {
                    "data": [{
                        "country_currency_desc": "Canada-Dollar",
                        "exchange_rate": "1.400",
                        "record_date": "2024-12-31"
                    }]
                }
                """;
        treasuryApi.enqueue(new MockResponse()
                .setBody(treasuryResponse)
                .addHeader("Content-Type", "application/json"));

        // Step 3: Retrieve the transaction with currency conversion
        mockMvc.perform(get("/api/transactions/{id}", stored.getId())
                        .param("currency", "Canada-Dollar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stored.getId().toString()))
                .andExpect(jsonPath("$.description").value("End-to-end test purchase"))
                .andExpect(jsonPath("$.originalPurchaseAmount").value(250.00))
                .andExpect(jsonPath("$.exchangeRate").value(1.400))
                .andExpect(jsonPath("$.convertedAmount").value(350.00))
                .andExpect(jsonPath("$.targetCurrency").value("Canada-Dollar"));
    }

    @Test
    @DisplayName("should return 422 when no exchange rate exists for the currency")
    void conversionFailsGracefully() throws Exception {
        // Store a transaction first
        String body = """
                {
                    "description": "No-rate test",
                    "transactionDate": "2025-01-15",
                    "purchaseAmount": 100.00
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        TransactionResponse stored = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TransactionResponse.class);

        // Treasury returns no data for this currency
        treasuryApi.enqueue(new MockResponse()
                .setBody("""
                        { "data": [] }
                        """)
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/transactions/{id}", stored.getId())
                        .param("currency", "Atlantis-Seashell"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("cannot be converted")));
    }
}
