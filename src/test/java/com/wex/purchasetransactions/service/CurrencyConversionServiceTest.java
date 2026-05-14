package com.wex.purchasetransactions.service;

import com.wex.purchasetransactions.dto.TreasuryApiResponse;
import com.wex.purchasetransactions.exception.ExchangeRateNotFoundException;
import com.wex.purchasetransactions.exception.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyConversionServiceTest {

    private MockWebServer mockServer;
    private CurrencyConversionService service;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        String baseUrl = mockServer.url("/").toString();
        service = new CurrencyConversionService(WebClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Nested
    @DisplayName("getExchangeRate")
    class GetExchangeRate {

        @Test
        @DisplayName("should return the exchange rate when the API has a matching record")
        void happyPath() throws InterruptedException {
            String responseBody = """
                    {
                        "data": [{
                            "country_currency_desc": "Canada-Dollar",
                            "exchange_rate": "1.362",
                            "record_date": "2024-12-31"
                        }]
                    }
                    """;

            mockServer.enqueue(new MockResponse()
                    .setBody(responseBody)
                    .addHeader("Content-Type", "application/json"));

            BigDecimal rate = service.getExchangeRate("Canada-Dollar", LocalDate.of(2025, 1, 15));

            assertThat(rate).isEqualByComparingTo("1.362");

            // Verify the request was constructed correctly
            RecordedRequest request = mockServer.takeRequest();
            String query = request.getRequestUrl().query();
            assertThat(query).contains("country_currency_desc:eq:Canada-Dollar");
            assertThat(query).contains("record_date:lte:2025-01-15");
            assertThat(query).contains("record_date:gte:2024-07-15");
            assertThat(query).contains("sort=-record_date");
            assertThat(query).satisfiesAnyOf(
                    q -> assertThat(q).contains("page%5Bsize%5D=1"),
                    q -> assertThat(q).contains("page[size]=1")
            );
        }

        @Test
        @DisplayName("should throw ExchangeRateNotFoundException when no rates exist in the window")
        void noRatesAvailable() {
            String emptyResponse = """
                    { "data": [] }
                    """;

            mockServer.enqueue(new MockResponse()
                    .setBody(emptyResponse)
                    .addHeader("Content-Type", "application/json"));

            assertThatThrownBy(() ->
                    service.getExchangeRate("Narnia-GoldCoin", LocalDate.of(2025, 3, 1)))
                    .isInstanceOf(ExchangeRateNotFoundException.class)
                    .hasMessageContaining("cannot be converted");
        }

        @Test
        @DisplayName("should throw ExternalApiException when the API returns an error")
        void apiError() {
            mockServer.enqueue(new MockResponse().setResponseCode(500));

            assertThatThrownBy(() ->
                    service.getExchangeRate("Canada-Dollar", LocalDate.of(2025, 1, 15)))
                    .isInstanceOf(ExternalApiException.class);
        }
    }

    @Nested
    @DisplayName("convertAmount")
    class ConvertAmount {

        @Test
        @DisplayName("should multiply and round to 2 decimal places")
        void standardConversion() {
            BigDecimal result = service.convertAmount(
                    new BigDecimal("200.00"), new BigDecimal("1.362"));

            assertThat(result).isEqualByComparingTo("272.40");
        }

        @Test
        @DisplayName("should handle rounding edge cases correctly")
        void roundingEdge() {
            // 100 * 1.345 = 134.5 -> 134.50
            BigDecimal result = service.convertAmount(
                    new BigDecimal("100.00"), new BigDecimal("1.345"));

            assertThat(result).isEqualByComparingTo("134.50");
        }

        @Test
        @DisplayName("should round up when the third decimal is 5 or above")
        void halfUp() {
            // 33.33 * 1.111 = 37.029963 -> 37.03
            BigDecimal result = service.convertAmount(
                    new BigDecimal("33.33"), new BigDecimal("1.111"));

            assertThat(result).isEqualByComparingTo("37.03");
        }
    }
}
