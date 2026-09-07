package com.finance.tracker.payment.service;

import com.finance.tracker.payment.enums.Provider;
import com.finance.tracker.payment.dto.PaymentProviderMetadata;
import com.finance.tracker.payment.exception.InvalidPaymentRequestException;
import com.finance.tracker.payment.provider.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderResolver {
    private final Map<Provider, PaymentProvider> providers = new EnumMap<>(Provider.class);

    public PaymentProviderResolver(List<PaymentProvider> registeredProviders) {
        registeredProviders.forEach(provider -> providers.put(provider.getProvider(), provider));
    }

    public PaymentProvider resolve(Provider provider) {
        PaymentProvider resolved = providers.get(provider);
        if (resolved == null || !resolved.isAvailable()) {
            throw new InvalidPaymentRequestException("Payment provider is not enabled: " + provider);
        }
        return resolved;
    }

    public java.util.List<PaymentProviderMetadata> availableProviders() {
        return providers.values().stream()
            .filter(PaymentProvider::isAvailable)
                .map(provider -> new PaymentProviderMetadata(
                        provider.getProvider(),
                        provider.getProvider() == Provider.PAYU ? "PayU" : "Dummy",
                        provider.supportedMethods()))
                .toList();
    }
}
