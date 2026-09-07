package com.finance.tracker.payment.service;

import com.finance.tracker.payment.config.PaymentOrderProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ConfiguredOrderAmountResolver implements OrderAmountResolver {
    private final PaymentOrderProperties properties;

    public ConfiguredOrderAmountResolver(PaymentOrderProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<BigDecimal> findAmount(String orderId, String customerId) {
        if (orderId == null || !orderId.equals(properties.getDemoOrderId()) || properties.getDemoAmount() == null) {
            return Optional.empty();
        }
        return Optional.of(properties.getDemoAmount());
    }
}
