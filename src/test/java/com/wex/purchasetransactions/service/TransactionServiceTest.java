package com.wex.purchasetransactions.service;

import com.wex.purchasetransactions.dto.ConvertedTransactionResponse;
import com.wex.purchasetransactions.dto.CreateTransactionRequest;
import com.wex.purchasetransactions.dto.TransactionResponse;
import com.wex.purchasetransactions.entity.PurchaseTransaction;
import com.wex.purchasetransactions.exception.TransactionNotFoundException;
import com.wex.purchasetransactions.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private CurrencyConversionService currencyService;

    @InjectMocks
    private TransactionService transactionService;

    @Nested
    @DisplayName("createTransaction")
    class CreateTransaction {

        @Test
        @DisplayName("should store a valid transaction and return it with an ID")
        void happyPath() {
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .description("Office supplies")
                    .transactionDate(LocalDate.of(2024, 12, 15))
                    .purchaseAmount(new BigDecimal("150.42"))
                    .build();

            UUID generatedId = UUID.randomUUID();
            PurchaseTransaction saved = PurchaseTransaction.builder()
                    .id(generatedId)
                    .description("Office supplies")
                    .transactionDate(LocalDate.of(2024, 12, 15))
                    .purchaseAmount(new BigDecimal("150.42"))
                    .build();

            when(repository.save(any(PurchaseTransaction.class))).thenReturn(saved);

            TransactionResponse response = transactionService.createTransaction(request);

            assertThat(response.getId()).isEqualTo(generatedId);
            assertThat(response.getDescription()).isEqualTo("Office supplies");
            assertThat(response.getPurchaseAmount()).isEqualByComparingTo("150.42");
            verify(repository).save(any(PurchaseTransaction.class));
        }

        @Test
        @DisplayName("should round the purchase amount to the nearest cent")
        void roundsToCent() {
            CreateTransactionRequest request = CreateTransactionRequest.builder()
                    .description("Rounding test")
                    .transactionDate(LocalDate.of(2024, 6, 1))
                    .purchaseAmount(new BigDecimal("100.455"))
                    .build();

            when(repository.save(any(PurchaseTransaction.class))).thenAnswer(invocation -> {
                PurchaseTransaction entity = invocation.getArgument(0);
                entity.setId(UUID.randomUUID());
                return entity;
            });

            TransactionResponse response = transactionService.createTransaction(request);

            // 100.455 rounds to 100.46 (HALF_UP)
            assertThat(response.getPurchaseAmount()).isEqualByComparingTo("100.46");
        }
    }

    @Nested
    @DisplayName("getTransactionWithConversion")
    class GetWithConversion {

        @Test
        @DisplayName("should retrieve and convert the transaction amount")
        void happyPath() {
            UUID id = UUID.randomUUID();
            PurchaseTransaction transaction = PurchaseTransaction.builder()
                    .id(id)
                    .description("Travel booking")
                    .transactionDate(LocalDate.of(2025, 1, 10))
                    .purchaseAmount(new BigDecimal("200.00"))
                    .build();

            when(repository.findById(id)).thenReturn(Optional.of(transaction));
            when(currencyService.getExchangeRate("Canada-Dollar", LocalDate.of(2025, 1, 10)))
                    .thenReturn(new BigDecimal("1.362"));
            when(currencyService.convertAmount(new BigDecimal("200.00"), new BigDecimal("1.362")))
                    .thenReturn(new BigDecimal("272.40"));

            ConvertedTransactionResponse response =
                    transactionService.getTransactionWithConversion(id, "Canada-Dollar");

            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getOriginalPurchaseAmount()).isEqualByComparingTo("200.00");
            assertThat(response.getExchangeRate()).isEqualByComparingTo("1.362");
            assertThat(response.getConvertedAmount()).isEqualByComparingTo("272.40");
            assertThat(response.getTargetCurrency()).isEqualTo("Canada-Dollar");
        }

        @Test
        @DisplayName("should throw TransactionNotFoundException for unknown ID")
        void notFound() {
            UUID unknownId = UUID.randomUUID();
            when(repository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    transactionService.getTransactionWithConversion(unknownId, "Canada-Dollar"))
                    .isInstanceOf(TransactionNotFoundException.class);
        }
    }
}
