package com.wex.purchasetransactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Maps the JSON shape returned by the Treasury Fiscal Data API.
 * Example response: { "data": [ { "country_currency_desc": "Canada-Dollar", "exchange_rate": "1.362", "record_date": "2024-12-31" } ] }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreasuryApiResponse {

    private List<ExchangeRateEntry> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeRateEntry {

        @JsonProperty("country_currency_desc")
        private String countryCurrencyDesc;

        @JsonProperty("exchange_rate")
        private String exchangeRate;

        @JsonProperty("record_date")
        private String recordDate;
    }
}
