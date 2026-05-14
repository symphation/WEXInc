package com.wex.purchasetransactions.repository;

import com.wex.purchasetransactions.entity.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<PurchaseTransaction, UUID> {
}
