package com.finance.tracker.payment.repository;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentRepository {
    Payment save(Payment payment);

    PaymentSession saveSession(PaymentSession session);

    Payment findById(String paymentId);

    Payment findByProviderPaymentId(String providerPaymentId);

    String findPaymentIdByIdempotencyKey(String idempotencyKey);

    PaymentSession findSessionByPaymentId(String paymentId);

    PaymentSession findSessionById(String sessionId);

    void recordEvent(String paymentId,
                     String eventType,
                     PaymentStatus status,
                     Provider provider,
                     String providerPaymentId,
                     String responseCode,
                     String responseMessage,
                     Map<String, String> safeDetails);
}
