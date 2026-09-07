package com.finance.tracker.payment.controller;

import com.finance.tracker.payment.config.PaymentProviderProperties;
import com.finance.tracker.payment.dto.PaymentResponse;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import com.finance.tracker.payment.provider.payu.PayUHashService;
import com.finance.tracker.payment.provider.payu.PayUStatusMapper;
import com.finance.tracker.payment.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/providers/payu")
public class PayUCallbackController {
    private final PaymentService paymentService;
    private final PaymentProviderProperties properties;
    private final PayUHashService hashService;
    private final PayUStatusMapper statusMapper;

    public PayUCallbackController(PaymentService paymentService,
                                  PaymentProviderProperties properties,
                                  PayUHashService hashService,
                                  PayUStatusMapper statusMapper) {
        this.paymentService = paymentService;
        this.properties = properties;
        this.hashService = hashService;
        this.statusMapper = statusMapper;
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> callback(@RequestParam Map<String, String> fields) {
        if (!properties.isEnabled() || !"test".equalsIgnoreCase(properties.getMode())
                || !hashService.verifyResponseHash(fields, properties.getSalt())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid PayU callback"));
        }

        String providerPaymentId = fields.get("txnid");
        String currency = fields.getOrDefault("currency", "INR");
        PaymentStatus status = statusMapper.toPaymentStatus(fields.get("status"));
        PaymentResponse response = paymentService.handleProviderCallback(
                Provider.PAYU,
                providerPaymentId,
                status,
                new BigDecimal(fields.get("amount")),
                currency);
        return ResponseEntity.ok(response);
    }
}
