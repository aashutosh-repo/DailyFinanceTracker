package com.finance.tracker.payment.provider;

import com.finance.tracker.payment.enums.PaymentStatus;

import java.util.Map;

public record PaymentInitiationResult(
        PaymentStatus status,
        String providerPaymentId,
        String message,
        String checkoutUrl,
        Map<String, String> checkoutFields) {
}
