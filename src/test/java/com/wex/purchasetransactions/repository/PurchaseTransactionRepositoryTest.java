package com.wex.purchasetransactions.repository;

import com.wex.purchasetransactions.entity.PurchaseTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PurchaseTransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Test
    @DisplayName("should persist a transaction and retrieve it by ID")
    void saveAndFindById() {
        PurchaseTransaction tx = PurchaseTransaction.builder()
                .description("Conference registration")
                .transactionDate(LocalDate.of(2025, 3, 20))
                .purchaseAmount(new BigDecimal("499.99"))
                .build();

        PurchaseTransaction saved = repository.save(tx);
        assertThat(saved.getId()).isNotNull();

        Optional<PurchaseTransaction> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Conference registration");
        assertThat(found.get().getPurchaseAmount()).isEqualByComparingTo("499.99");
    }

    @Test
    @DisplayName("should return empty for non-existent ID")
    void findByIdNotFound() {
        Optional<PurchaseTransaction> found = repository.findById(java.util.UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should preserve monetary precision")
    void preservesPrecision() {
        PurchaseTransaction tx = PurchaseTransaction.builder()
                .description("Precision test")
                .transactionDate(LocalDate.of(2025, 1, 1))
                .purchaseAmount(new BigDecimal("0.01"))
                .build();

        PurchaseTransaction saved = repository.save(tx);
        PurchaseTransaction found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPurchaseAmount()).isEqualByComparingTo("0.01");
    }
}
