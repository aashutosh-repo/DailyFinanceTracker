package com.finance.tracker.payment.provider.payu;

import com.finance.tracker.payment.config.PaymentProviderProperties;
import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import com.finance.tracker.payment.exception.InvalidPaymentRequestException;
import com.finance.tracker.payment.provider.PaymentInitiationResult;
import com.finance.tracker.payment.provider.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.math.RoundingMode;

@Component
public class PayUPaymentProvider implements PaymentProvider {
    private final PaymentProviderProperties properties;
    private final PayUHashService hashService;

    public PayUPaymentProvider(PaymentProviderProperties properties, PayUHashService hashService) {
        this.properties = properties;
        this.hashService = hashService;
    }

    @Override
    public PaymentInitiationResult initiatePayment(Payment payment) {
        validateConfiguration();

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("key", properties.getKey());
        fields.put("txnid", payment.getPaymentId());
        fields.put("amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("productinfo", "Finance Tracker payment");
        fields.put("firstname", "Customer");
        fields.put("email", "customer@example.com");
        fields.put("phone", "9999999999");
        fields.put("surl", properties.getSuccessUrl());
        fields.put("furl", properties.getFailureUrl());
        fields.put("udf1", "");
        fields.put("udf2", "");
        fields.put("udf3", "");
        fields.put("udf4", "");
        fields.put("udf5", "");
        fields.put("hash", hashService.generateRequestHash(fields, properties.getSalt()));

        return new PaymentInitiationResult(
                PaymentStatus.INITIATED,
                payment.getPaymentId(),
                "Redirecting to PayU test checkout",
                properties.getBaseUrl(),
                fields);
    }

    @Override
    public Provider getProvider() {
        return Provider.PAYU;
    }

    @Override
    public boolean isAvailable() {
        return properties.isEnabled()
                && "test".equalsIgnoreCase(properties.getMode())
                && !isBlank(properties.getKey())
                && !isBlank(properties.getSalt())
                && !isBlank(properties.getBaseUrl())
                && !isBlank(properties.getSuccessUrl())
                && !isBlank(properties.getFailureUrl());
    }

    @Override
    public Set<PaymentMethod> supportedMethods() {
        return Set.of(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.NET_BANKING, PaymentMethod.WALLET);
    }

    private void validateConfiguration() {
        if (!properties.isEnabled() || !"test".equalsIgnoreCase(properties.getMode())) {
            throw new InvalidPaymentRequestException("PayU test provider is not enabled");
        }
        if (isBlank(properties.getKey()) || isBlank(properties.getSalt()) || isBlank(properties.getBaseUrl())
                || isBlank(properties.getSuccessUrl()) || isBlank(properties.getFailureUrl())) {
            throw new InvalidPaymentRequestException("PayU test configuration is incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
