package com.wex.purchasetransactions.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI purchaseTransactionsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WEX Purchase Transactions API")
                        .version("1.0.0")
                        .description("""
                                A REST API for storing and retrieving purchase transactions with real-time \
                                currency conversion powered by the **U.S. Treasury Reporting Rates of Exchange**.

                                ### How it works
                                1. **Store** a transaction — you get back a unique ID
                                2. **Retrieve** any transaction and pass a target currency — the API looks up \
                                the most recent Treasury exchange rate within 6 months of the purchase date \
                                and returns the converted amount

                                ### Currency format
                                Currencies use the Treasury API's `country-currency` format, for example:
                                - `Canada-Dollar`
                                - `Euro Zone-Euro`
                                - `Japan-Yen`
                                - `United Kingdom-Pound`

                                Browse the full list at the \
                                [Treasury Fiscal Data site](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/).
                                """)
                        .contact(new Contact()
                                .name("WEX Corporate Payments")
                                .url("https://www.wexinc.com"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")))
                .tags(List.of(
                        new Tag()
                                .name("Purchase Transactions")
                                .description("Operations for storing and retrieving purchase transactions")))
                .components(new Components()
                        .examples(Map.of(
                                "CreateTransactionRequest_Lunch",
                                new Example()
                                        .summary("Team lunch")
                                        .value("""
                                                {
                                                  "description": "Team lunch at downtown bistro",
                                                  "transactionDate": "2025-06-01",
                                                  "purchaseAmount": 87.50
                                                }"""),

                                "CreateTransactionRequest_Software",
                                new Example()
                                        .summary("Software subscription")
                                        .value("""
                                                {
                                                  "description": "Monthly SaaS subscription renewal",
                                                  "transactionDate": "2025-09-15",
                                                  "purchaseAmount": 299.00
                                                }"""),

                                "TransactionResponse_Created",
                                new Example()
                                        .summary("Stored transaction")
                                        .value("""
                                                {
                                                  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                  "description": "Team lunch at downtown bistro",
                                                  "transactionDate": "2025-06-01",
                                                  "purchaseAmount": 87.50
                                                }"""),

                                "ConvertedTransactionResponse_CAD",
                                new Example()
                                        .summary("Converted to Canadian Dollar")
                                        .value("""
                                                {
                                                  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                  "description": "Team lunch at downtown bistro",
                                                  "transactionDate": "2025-06-01",
                                                  "originalPurchaseAmount": 87.50,
                                                  "exchangeRate": 1.435,
                                                  "convertedAmount": 125.56,
                                                  "targetCurrency": "Canada-Dollar"
                                                }"""),

                                "ConvertedTransactionResponse_EUR",
                                new Example()
                                        .summary("Converted to Euro")
                                        .value("""
                                                {
                                                  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                  "description": "Team lunch at downtown bistro",
                                                  "transactionDate": "2025-06-01",
                                                  "originalPurchaseAmount": 87.50,
                                                  "exchangeRate": 0.924,
                                                  "convertedAmount": 80.85,
                                                  "targetCurrency": "Euro Zone-Euro"
                                                }"""),

                                "ErrorResponse_NotFound",
                                new Example()
                                        .summary("Transaction not found")
                                        .value("""
                                                {
                                                  "status": 404,
                                                  "error": "Not Found",
                                                  "message": "Transaction not found with id: a1b2c3d4-...",
                                                  "timestamp": "2025-06-01T14:30:00"
                                                }"""),

                                "ErrorResponse_RateUnavailable",
                                new Example()
                                        .summary("No exchange rate available")
                                        .value("""
                                                {
                                                  "status": 404,
                                                  "error": "Not Found",
                                                  "message": "No exchange rate found for currency 'Japan-Yen' within 6 months of 2020-01-01",
                                                  "timestamp": "2025-06-01T14:30:00"
                                                }"""),

                                "ErrorResponse_Validation",
                                new Example()
                                        .summary("Validation failure")
                                        .value("""
                                                {
                                                  "status": 400,
                                                  "error": "Bad Request",
                                                  "message": "Validation failed",
                                                  "timestamp": "2025-06-01T14:30:00",
                                                  "fieldErrors": {
                                                    "description": "Description must not exceed 50 characters",
                                                    "purchaseAmount": "Purchase amount must be a positive value"
                                                  }
                                                }""")))
                );
    }
}
