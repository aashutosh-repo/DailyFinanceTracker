package com.finance.tracker.payment.dto;

import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.Provider;

import java.util.Set;

public record PaymentProviderMetadata(
        Provider provider,
        String displayName,
        Set<PaymentMethod> supportedMethods) {
}
