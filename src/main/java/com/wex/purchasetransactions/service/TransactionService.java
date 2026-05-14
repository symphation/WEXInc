package com.wex.purchasetransactions.service;

import com.wex.purchasetransactions.dto.ConvertedTransactionResponse;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import com.wex.purchasetransactions.entity.PurchaseTransaction;
import com.wex.purchasetransactions.exception.TransactionNotFoundException;
import com.wex.purchasetransactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final CurrencyConversionService currencyService;

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Round to nearest cent before persisting
        BigDecimal roundedAmount = request.getPurchaseAmount()
                .setScale(2, RoundingMode.HALF_UP);

        PurchaseTransaction entity = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .purchaseAmount(roundedAmount)
                .build();

        PurchaseTransaction saved = repository.save(entity);
        log.info("Stored transaction {} for ${}", saved.getId(), roundedAmount);

        return toResponse(saved);
    }

    public ConvertedTransactionResponse getTransactionWithConversion(UUID id, String currency) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        BigDecimal exchangeRate = currencyService.getExchangeRate(
                currency, transaction.getTransactionDate());

        BigDecimal convertedAmount = currencyService.convertAmount(
                transaction.getPurchaseAmount(), exchangeRate);

        return ConvertedTransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .originalPurchaseAmount(transaction.getPurchaseAmount())
                .exchangeRate(exchangeRate)
                .convertedAmount(convertedAmount)
                .targetCurrency(currency)
                .build();
    }

    private TransactionResponse toResponse(PurchaseTransaction entity) {
        return TransactionResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .purchaseAmount(entity.getPurchaseAmount())
                .build();
    }
}
