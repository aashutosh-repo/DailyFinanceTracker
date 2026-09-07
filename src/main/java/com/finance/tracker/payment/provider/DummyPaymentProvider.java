package com.finance.tracker.payment.provider;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import com.finance.tracker.payment.enums.PaymentSessionStatus;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Component
public class DummyPaymentProvider implements PaymentProvider {

    @Override
    public PaymentInitiationResult initiatePayment(Payment payment) {
        PaymentProcessingResult result = processPayment(payment);
        return new PaymentInitiationResult(
                result.status(),
                result.providerPaymentId(),
                result.message(),
                null,
                java.util.Map.of());
    }

    @Override
    public PaymentProcessingResult processPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentProcessingResult(
                    PaymentStatus.FAILED,
                    "DUMMY-FAIL-" + System.currentTimeMillis(),
                    "Payment amount is invalid"
            );
        }

        String orderId = payment.getOrderId() == null ? "" : payment.getOrderId().toUpperCase(Locale.ROOT);
        boolean shouldFail = orderId.contains("FAIL") || orderId.contains("DECLINE");

        if (shouldFail) {
            return new PaymentProcessingResult(
                    PaymentStatus.FAILED,
                    "DUMMY-FAIL-" + System.currentTimeMillis(),
                    "Payment failed in dummy provider"
            );
        }

        return new PaymentProcessingResult(
                PaymentStatus.SUCCESS,
                "DUMMY-" + System.currentTimeMillis(),
                "Payment successful"
        );
    }

    @Override
    public PaymentSession createUpiSession(Payment payment, String upiId) {
        String normalizedUpi = (upiId == null ? "customer@upi" : upiId.trim());
        String orderId = payment.getOrderId() == null ? "ORD-NONE" : payment.getOrderId();
        String amount = payment.getAmount() == null ? "0.00" : payment.getAmount().toPlainString();
        String payload = "upi://pay?pa=merchant@upi&pn=DemoMerchant&am=" + amount + "&cu=" + payment.getCurrency() + "&tr=" + payment.getPaymentId();

        PaymentSession session = new PaymentSession();
        session.setSessionId("SESSION-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        session.setPaymentId(payment.getPaymentId());
        session.setOrderId(orderId);
        session.setAmount(payment.getAmount());
        session.setCurrency(payment.getCurrency());
        session.setPaymentMethod(payment.getPaymentMethod());
        session.setUpiId(normalizedUpi);
        session.setUpiUri(payload);
        session.setQrPayload(payload);
        session.setStatus(PaymentSessionStatus.AWAITING_PAYMENT);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        if (normalizedUpi.equalsIgnoreCase("failed@dummy")) {
            session.setStatus(PaymentSessionStatus.FAILED);
        } else if (normalizedUpi.equalsIgnoreCase("expired@dummy")) {
            session.setStatus(PaymentSessionStatus.EXPIRED);
        } else if (normalizedUpi.equalsIgnoreCase("success@dummy")) {
            session.setStatus(PaymentSessionStatus.SUCCESS);
        }

        return session;
    }

    @Override
    public PaymentProviderStatusResponse getPaymentStatus(String paymentId) {
        String normalized = paymentId == null ? "" : paymentId.trim();
        if (normalized.contains("FAIL")) {
            return new PaymentProviderStatusResponse("FAILED", "Dummy provider reported failure");
        }
        if (normalized.contains("EXPIRE")) {
            return new PaymentProviderStatusResponse("EXPIRED", "Dummy provider reported expiry");
        }
        return new PaymentProviderStatusResponse("SUCCESS", "Dummy provider reported success");
    }

    @Override
    public Provider getProvider() {
        return Provider.DUMMY;
    }

    @Override
    public String getProviderName() {
        return Provider.DUMMY.name();
    }
}
