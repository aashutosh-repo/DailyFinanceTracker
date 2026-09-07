package com.finance.tracker.payment.provider.payu;

import com.finance.tracker.payment.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayUStatusMapperTest {
    private final PayUStatusMapper mapper = new PayUStatusMapper();

    @Test
    void mapsKnownProviderStatuses() {
        assertEquals(PaymentStatus.SUCCESS, mapper.toPaymentStatus("success"));
        assertEquals(PaymentStatus.FAILED, mapper.toPaymentStatus("failure"));
        assertEquals(PaymentStatus.PENDING, mapper.toPaymentStatus("pending"));
        assertEquals(PaymentStatus.EXPIRED, mapper.toPaymentStatus("expired"));
    }

    @Test
    void mapsUnknownStatusToProcessing() {
        assertEquals(PaymentStatus.PROCESSING, mapper.toPaymentStatus("provider-new-status"));
    }
}
