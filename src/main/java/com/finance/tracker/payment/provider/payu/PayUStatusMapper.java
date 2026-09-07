package com.finance.tracker.payment.provider.payu;

import com.finance.tracker.payment.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PayUStatusMapper {
    public PaymentStatus toPaymentStatus(String providerStatus) {
        if (providerStatus == null) {
            return PaymentStatus.PROCESSING;
        }
        return switch (providerStatus.trim().toLowerCase(Locale.ROOT)) {
            case "success" -> PaymentStatus.SUCCESS;
            case "failure", "failed", "cancelled" -> PaymentStatus.FAILED;
            case "pending", "in-progress" -> PaymentStatus.PENDING;
            case "expired" -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.PROCESSING;
        };
    }
}
