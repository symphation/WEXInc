package com.wex.purchasetransactions.exception;

public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String currency, String transactionDate) {
        super("The purchase cannot be converted to the target currency. "
                + "No exchange rate is available for " + currency
                + " within 6 months of the transaction date " + transactionDate + ".");
    }
}
