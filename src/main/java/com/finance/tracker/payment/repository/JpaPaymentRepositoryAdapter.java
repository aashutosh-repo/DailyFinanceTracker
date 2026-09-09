package com.finance.tracker.payment.repository;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import com.finance.tracker.payment.persistence.PaymentEventEntity;
import com.finance.tracker.payment.persistence.PaymentRecordEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Primary
public class JpaPaymentRepositoryAdapter implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentEventJpaRepository eventJpaRepository;
    private final Map<String, PaymentSession> sessionsByPaymentId = new ConcurrentHashMap<>();
    private final Map<String, PaymentSession> sessionsBySessionId = new ConcurrentHashMap<>();

    public JpaPaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository,
                                       PaymentEventJpaRepository eventJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(PaymentRecordEntity.fromDomain(payment)).toDomain();
    }

    @Override
    public PaymentSession saveSession(PaymentSession session) {
        sessionsBySessionId.put(session.getSessionId(), session);
        sessionsByPaymentId.put(session.getPaymentId(), session);
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(String paymentId) {
        return paymentJpaRepository.findById(paymentId).map(PaymentRecordEntity::toDomain).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByProviderPaymentId(String providerPaymentId) {
        return paymentJpaRepository.findByProviderPaymentId(providerPaymentId)
                .map(PaymentRecordEntity::toDomain)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String findPaymentIdByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(PaymentRecordEntity::getPaymentId)
                .orElse(null);
    }

    @Override
    public PaymentSession findSessionByPaymentId(String paymentId) {
        return sessionsByPaymentId.get(paymentId);
    }

    @Override
    public PaymentSession findSessionById(String sessionId) {
        return sessionsBySessionId.get(sessionId);
    }

    @Override
    @Transactional
    public void recordEvent(String paymentId, String eventType, PaymentStatus status, Provider provider,
                            String providerPaymentId, String responseCode, String responseMessage,
                            Map<String, String> safeDetails) {
        eventJpaRepository.save(PaymentEventEntity.create(
                paymentId, eventType, status, provider, providerPaymentId,
                responseCode, responseMessage, safeDetails));
    }
}
