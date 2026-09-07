package com.finance.tracker.payment.provider;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;

import java.util.Set;

public interface PaymentProvider {
    PaymentInitiationResult initiatePayment(Payment payment);

    default PaymentProcessingResult processPayment(Payment payment) {
        throw new UnsupportedOperationException("Synchronous processing is not supported by " + getProviderName());
    }

    default PaymentSession createUpiSession(Payment payment, String upiId) {
        throw new UnsupportedOperationException("UPI sessions are not supported by " + getProviderName());
    }

    default PaymentProviderStatusResponse getPaymentStatus(String paymentId) {
        throw new UnsupportedOperationException("Status queries are not supported by " + getProviderName());
    }

    Provider getProvider();

    default boolean isAvailable() {
        return true;
    }

    default void verifyCallback(String paymentId, PaymentStatus status) {
        // Provider-specific callback verification belongs in the adapter.
    }

    default Set<PaymentMethod> supportedMethods() {
        return Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING, PaymentMethod.WALLET);
    }

    default String getProviderName() {
        return getProvider().name();
    }
}
