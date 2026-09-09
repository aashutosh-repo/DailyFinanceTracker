package com.finance.tracker.payment.repository;

import com.finance.tracker.payment.persistence.PaymentRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentRecordEntity, String> {
    Optional<PaymentRecordEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentRecordEntity> findByProviderPaymentId(String providerPaymentId);
}
