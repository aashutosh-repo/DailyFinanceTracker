package com.finance.tracker.repository;
import com.finance.tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserIdAndTxnDateBetween(UUID userId, LocalDate from, LocalDate to);
}
