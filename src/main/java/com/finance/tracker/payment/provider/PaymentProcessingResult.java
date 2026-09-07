package com.finance.tracker.payment.provider;

import com.finance.tracker.payment.enums.PaymentStatus;

public record PaymentProcessingResult(
        PaymentStatus status,
        String providerPaymentId,
        String message
) {
}
