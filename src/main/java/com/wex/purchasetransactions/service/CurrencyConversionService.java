package com.wex.purchasetransactions.service;

import com.wex.purchasetransactions.dto.TreasuryApiResponse;
import com.wex.purchasetransactions.exception.ExchangeRateNotFoundException;
import com.wex.purchasetransactions.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CurrencyConversionService {

    private final WebClient webClient;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public CurrencyConversionService(
            WebClient.Builder webClientBuilder,
            @Value("${treasury.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Looks up the most recent exchange rate for the given currency that falls
     * on or before the transaction date, within a 6-month lookback window.
     * Returns the rate as a BigDecimal.
     */
    public BigDecimal getExchangeRate(String currency, LocalDate transactionDate) {
        LocalDate sixMonthsBefore = transactionDate.minusMonths(6);

        // Build the filter to grab rates within the valid window, newest first
        String filter = String.format(
                "country_currency_desc:eq:%s,record_date:lte:%s,record_date:gte:%s",
                currency,
                transactionDate.format(DATE_FMT),
                sixMonthsBefore.format(DATE_FMT)
        );

        log.debug("Querying Treasury API: currency={}, dateRange=[{}, {}]",
                currency, sixMonthsBefore, transactionDate);

        TreasuryApiResponse response;
        try {
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("fields", "country_currency_desc,exchange_rate,record_date")
                            .queryParam("filter", filter)
                            .queryParam("sort", "-record_date")
                            .queryParam("page[size]", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(TreasuryApiResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new ExternalApiException(
                    "Treasury API returned HTTP " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            throw new ExternalApiException(
                    "Failed to reach the Treasury API: " + ex.getMessage(), ex);
        }

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new ExchangeRateNotFoundException(currency, transactionDate.toString());
        }

        String rateStr = response.getData().get(0).getExchangeRate();
        log.info("Found exchange rate {} for {} (record_date={})",
                rateStr, currency, response.getData().get(0).getRecordDate());

        return new BigDecimal(rateStr);
    }

    /**
     * Converts the original USD amount using the provided exchange rate,
     * rounding to two decimal places.
     */
    public BigDecimal convertAmount(BigDecimal originalAmount, BigDecimal exchangeRate) {
        return originalAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }
}
