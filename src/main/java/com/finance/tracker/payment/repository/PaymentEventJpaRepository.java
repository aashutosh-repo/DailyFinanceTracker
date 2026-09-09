package com.finance.tracker.payment.repository;

import com.finance.tracker.payment.persistence.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventJpaRepository extends JpaRepository<PaymentEventEntity, Long> {
}
