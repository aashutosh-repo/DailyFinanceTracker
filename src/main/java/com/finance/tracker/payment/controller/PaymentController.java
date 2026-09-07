package com.finance.tracker.payment.controller;

import com.finance.tracker.payment.dto.CreatePaymentRequest;
import com.finance.tracker.payment.dto.PaymentResponse;
import com.finance.tracker.payment.dto.PaymentSessionResponse;
import com.finance.tracker.payment.dto.PaymentProviderMetadata;
import com.finance.tracker.payment.service.PaymentProviderResolver;
import com.finance.tracker.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentProviderResolver paymentProviderResolver;

    public PaymentController(PaymentService paymentService, PaymentProviderResolver paymentProviderResolver) {
        this.paymentService = paymentService;
        this.paymentProviderResolver = paymentProviderResolver;
    }

    @GetMapping("/providers")
    public ResponseEntity<java.util.List<PaymentProviderMetadata>> getProviders() {
        return ResponseEntity.ok(paymentProviderResolver.availableProviders());
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/upi-session")
    public ResponseEntity<PaymentSessionResponse> createUpiSession(
            @PathVariable String paymentId,
            @RequestBody java.util.Map<String, String> request) {
        String upiId = request == null ? null : request.get("upiId");
        return ResponseEntity.ok(paymentService.createUpiSession(paymentId, upiId));
    }

    @GetMapping("/{paymentId}/session")
    public ResponseEntity<PaymentSessionResponse> getPaymentSession(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentSession(paymentId));
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.checkPaymentStatus(paymentId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
