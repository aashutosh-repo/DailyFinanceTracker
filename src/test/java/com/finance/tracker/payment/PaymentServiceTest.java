package com.finance.tracker.payment;

import com.finance.tracker.payment.dto.CreatePaymentRequest;
import com.finance.tracker.payment.dto.PaymentResponse;
import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.exception.InvalidPaymentRequestException;
import com.finance.tracker.payment.exception.PaymentNotFoundException;
import com.finance.tracker.payment.provider.DummyPaymentProvider;
import com.finance.tracker.payment.repository.InMemoryPaymentRepository;
import com.finance.tracker.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(new InMemoryPaymentRepository(), new DummyPaymentProvider());
    }

    @Test
    void shouldCreateSuccessfulPayment() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ORD-SUCCESS-001");
        request.setAmount(new BigDecimal("1768.82"));
        request.setCurrency("INR");
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setIdempotencyKey("idem-success-1");

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("ORD-SUCCESS-001", response.getOrderId());
        assertEquals("DUMMY", response.getProvider());
        assertNotNull(response.getPaymentId());
    }

    @Test
    void shouldMarkFailedPaymentWhenProviderFails() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ORD-FAIL-001");
        request.setAmount(new BigDecimal("1768.82"));
        request.setCurrency("INR");
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setIdempotencyKey("idem-fail-1");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertNotNull(response.getProviderPaymentId());
    }

    @Test
    void shouldRejectDuplicateIdempotencyKey() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ORD-DUP-001");
        request.setAmount(new BigDecimal("1768.82"));
        request.setCurrency("INR");
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setIdempotencyKey("idem-duplicate");

        PaymentResponse first = paymentService.createPayment(request);
        PaymentResponse second = paymentService.createPayment(request);

        assertEquals(first.getPaymentId(), second.getPaymentId());
        assertEquals(first.getStatus(), second.getStatus());
    }

    @Test
    void shouldRejectInvalidPaymentRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(" ");
        request.setAmount(BigDecimal.ZERO);
        request.setCurrency(" ");
        request.setPaymentMethod(null);
        request.setIdempotencyKey(" ");

        assertThrows(InvalidPaymentRequestException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void shouldRejectInvalidStateTransition() {
        Payment payment = new Payment();
        payment.setPaymentId("PAY-STATE");
        payment.setStatus(PaymentStatus.SUCCESS);

        assertThrows(IllegalStateException.class, () -> payment.transitionTo(PaymentStatus.PROCESSING));
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPayment("PAY-UNKNOWN"));
    }

    @Test
    void shouldFailProviderWhenRequestAmountIsZero() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("ORD-ZERO-001");
        request.setAmount(BigDecimal.ZERO);
        request.setCurrency("INR");
        request.setPaymentMethod(PaymentMethod.UPI);
        request.setIdempotencyKey("idem-zero");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
    }
}
