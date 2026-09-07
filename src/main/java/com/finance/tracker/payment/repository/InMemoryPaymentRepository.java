package com.finance.tracker.payment.repository;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPaymentRepository {
    private final Map<String, Payment> paymentsById = new ConcurrentHashMap<>();
    private final Map<String, String> idemKeyToPaymentId = new ConcurrentHashMap<>();
    private final Map<String, PaymentSession> sessionsByPaymentId = new ConcurrentHashMap<>();
    private final Map<String, PaymentSession> sessionsBySessionId = new ConcurrentHashMap<>();

    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        paymentsById.put(payment.getPaymentId(), payment);
        if (payment.getIdempotencyKey() != null) {
            idemKeyToPaymentId.put(payment.getIdempotencyKey(), payment.getPaymentId());
        }
        return payment;
    }

    public PaymentSession saveSession(PaymentSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Payment session cannot be null");
        }
        sessionsBySessionId.put(session.getSessionId(), session);
        if (session.getPaymentId() != null) {
            sessionsByPaymentId.put(session.getPaymentId(), session);
        }
        return session;
    }

    public Payment findById(String paymentId) {
        return paymentsById.get(paymentId);
    }

    public Payment findByProviderPaymentId(String providerPaymentId) {
        return paymentsById.values().stream()
                .filter(payment -> providerPaymentId != null && providerPaymentId.equals(payment.getProviderPaymentId()))
                .findFirst()
                .orElse(null);
    }

    public String findPaymentIdByIdempotencyKey(String idempotencyKey) {
        return idemKeyToPaymentId.get(idempotencyKey);
    }

    public PaymentSession findSessionByPaymentId(String paymentId) {
        return sessionsByPaymentId.get(paymentId);
    }

    public PaymentSession findSessionById(String sessionId) {
        return sessionsBySessionId.get(sessionId);
    }
}
