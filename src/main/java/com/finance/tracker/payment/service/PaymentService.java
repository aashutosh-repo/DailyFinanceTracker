package com.finance.tracker.payment.service;

import com.finance.tracker.payment.dto.CreatePaymentRequest;
import com.finance.tracker.payment.dto.PaymentResponse;
import com.finance.tracker.payment.dto.PaymentSessionResponse;
import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.entity.PaymentSession;
import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentSessionStatus;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import com.finance.tracker.payment.exception.InvalidPaymentRequestException;
import com.finance.tracker.payment.exception.PaymentNotFoundException;
import com.finance.tracker.payment.notification.DummyPaymentNotificationService;
import com.finance.tracker.payment.provider.DummyPaymentProvider;
import com.finance.tracker.payment.provider.PaymentInitiationResult;
import com.finance.tracker.payment.provider.PaymentProvider;
import com.finance.tracker.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProviderResolver paymentProviderResolver;
    private final DummyPaymentNotificationService paymentNotificationService;
    private final OrderAmountResolver orderAmountResolver;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          PaymentProviderResolver paymentProviderResolver,
                          DummyPaymentNotificationService paymentNotificationService,
                          OrderAmountResolver orderAmountResolver) {
        this.paymentRepository = paymentRepository;
        this.paymentProviderResolver = paymentProviderResolver;
        this.paymentNotificationService = paymentNotificationService;
        this.orderAmountResolver = orderAmountResolver;
    }

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentProviderResolver paymentProviderResolver,
                          DummyPaymentNotificationService paymentNotificationService) {
        this(paymentRepository, paymentProviderResolver, paymentNotificationService, null);
    }

    public PaymentService(PaymentRepository paymentRepository, DummyPaymentProvider dummyPaymentProvider) {
        this(paymentRepository,
                new PaymentProviderResolver(java.util.List.of(dummyPaymentProvider)),
                new DummyPaymentNotificationService());
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        validateRequest(request);

        String existingPaymentId = paymentRepository.findPaymentIdByIdempotencyKey(request.getIdempotencyKey());
        if (existingPaymentId != null) {
            Payment existing = paymentRepository.findById(existingPaymentId);
            if (existing != null) {
                return PaymentResponse.fromEntity(existing, "Payment already processed for this idempotency key");
            }
        }

        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId());
        payment.setOrderId(request.getOrderId().trim());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().trim().toUpperCase());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setIdempotencyKey(request.getIdempotencyKey().trim());
        Provider requestedProvider = request.getProvider() == null ? Provider.DUMMY : request.getProvider();
        if (requestedProvider == Provider.PAYU) {
            if (orderAmountResolver == null) {
                throw new InvalidPaymentRequestException("PayU order validation is not configured");
            }
            BigDecimal orderAmount = orderAmountResolver.findAmount(request.getOrderId().trim(), "customer-demo")
                    .orElseThrow(() -> new InvalidPaymentRequestException("Order was not found or is not payable"));
            if (orderAmount.compareTo(request.getAmount()) != 0) {
                throw new InvalidPaymentRequestException("Payment amount does not match the order");
            }
        }
        PaymentProvider paymentProvider = paymentProviderResolver.resolve(requestedProvider);
        if (request.getPaymentMethod() != null && !paymentProvider.supportedMethods().contains(request.getPaymentMethod())) {
            throw new InvalidPaymentRequestException("Payment method is not supported by " + requestedProvider);
        }
        payment.setProvider(requestedProvider);
        payment.setCustomerId("customer-demo");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        paymentRepository.recordEvent(payment.getPaymentId(), "PAYMENT_REQUEST_CREATED", payment.getStatus(),
            requestedProvider, null, null, "Payment request accepted", java.util.Map.of(
                "orderId", payment.getOrderId(),
                "paymentMethod", payment.getPaymentMethod() == null ? "PROVIDER_SELECTED" : payment.getPaymentMethod().name(),
                "currency", payment.getCurrency()));

        PaymentInitiationResult initiationResult = paymentProvider.initiatePayment(payment);
        if (initiationResult.status() == PaymentStatus.SUCCESS || initiationResult.status() == PaymentStatus.FAILED) {
            payment.transitionTo(PaymentStatus.PROCESSING);
            payment.transitionTo(initiationResult.status());
        } else {
            payment.transitionTo(initiationResult.status());
        }
        payment.setProviderPaymentId(initiationResult.providerPaymentId());
        paymentRepository.save(payment);
        paymentRepository.recordEvent(payment.getPaymentId(), "PROVIDER_INITIATION_RESPONSE", payment.getStatus(),
            requestedProvider, initiationResult.providerPaymentId(), null, initiationResult.message(),
            java.util.Map.of("hasCheckoutRedirect", String.valueOf(initiationResult.checkoutUrl() != null)));

        if (initiationResult.status() == PaymentStatus.SUCCESS || initiationResult.status() == PaymentStatus.FAILED) {
            return PaymentResponse.fromEntity(payment, initiationResult.message(), initiationResult.checkoutUrl(), initiationResult.checkoutFields());
        }
        return PaymentResponse.fromEntity(payment, initiationResult.message(), initiationResult.checkoutUrl(), initiationResult.checkoutFields());
    }

    public PaymentSessionResponse createUpiSession(String paymentId, String upiId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new InvalidPaymentRequestException("Payment ID is required");
        }

        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(paymentId);
        }

        if (payment.getPaymentMethod() != PaymentMethod.UPI) {
            throw new InvalidPaymentRequestException("This payment is not configured for UPI");
        }

        PaymentProvider paymentProvider = paymentProviderResolver.resolve(payment.getProvider());
        PaymentSession session = paymentProvider.createUpiSession(payment, upiId);
        paymentRepository.saveSession(session);

        paymentNotificationService.sendEmailNotification(payment.getPaymentId(), "upi@dummy-finance.local", "UPI payment request created for INR " + payment.getAmount());
        paymentNotificationService.sendSmsNotification(payment.getPaymentId(), "+91 99999 99999", "Your UPI payment request is ready. Pay before it expires.");

        return PaymentSessionResponse.fromEntity(session);
    }

    public PaymentSessionResponse getPaymentSession(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new InvalidPaymentRequestException("Payment ID is required");
        }

        PaymentSession session = paymentRepository.findSessionByPaymentId(paymentId);
        if (session == null) {
            throw new PaymentNotFoundException("No payment session found for payment " + paymentId);
        }
        return PaymentSessionResponse.fromEntity(session);
    }

    public PaymentResponse getPayment(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new InvalidPaymentRequestException("Payment ID is required");
        }

        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(paymentId);
        }

        return PaymentResponse.fromEntity(payment, "Payment fetched successfully");
    }

    public PaymentResponse checkPaymentStatus(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException(paymentId);
        }

        PaymentSession session = paymentRepository.findSessionByPaymentId(paymentId);
        if (session != null && session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(PaymentSessionStatus.EXPIRED);
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            paymentRepository.saveSession(session);
            return PaymentResponse.fromEntity(payment, "Payment expired; retry with a new request.");
        }

        if (session != null && session.getStatus() == PaymentSessionStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return PaymentResponse.fromEntity(payment, "Payment captured successfully via UPI");
        }

        if (session != null && session.getStatus() == PaymentSessionStatus.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return PaymentResponse.fromEntity(payment, "Payment failed in UPI session");
        }

        return PaymentResponse.fromEntity(payment, "Payment status checked successfully");
    }

    @Transactional
    public PaymentResponse handleProviderCallback(Provider provider,
                                                   String providerPaymentId,
                                                   PaymentStatus nextStatus,
                                                   BigDecimal amount,
                                                   String currency,
                                                   String responseCode,
                                                   String responseMessage) {
        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId);
        if (payment == null || payment.getProvider() != provider) {
            throw new InvalidPaymentRequestException("Unknown provider payment reference");
        }
        if (amount == null || payment.getAmount().compareTo(amount) != 0) {
            throw new InvalidPaymentRequestException("Payment amount does not match the provider callback");
        }
        if (currency == null || !payment.getCurrency().equalsIgnoreCase(currency)) {
            throw new InvalidPaymentRequestException("Payment currency does not match the provider callback");
        }

        boolean duplicateCallback = payment.getStatus() == nextStatus;
        if (!duplicateCallback) {
            payment.transitionTo(nextStatus);
            paymentRepository.save(payment);
        }
        paymentRepository.recordEvent(payment.getPaymentId(), "PROVIDER_CALLBACK_RECEIVED", payment.getStatus(),
            provider, providerPaymentId, responseCode, responseMessage, java.util.Map.of(
                "currency", currency,
                "status", nextStatus.name(),
                        "duplicate", String.valueOf(duplicateCallback)));
        return PaymentResponse.fromEntity(payment, "Provider callback reconciled");
    }

    private void validateRequest(CreatePaymentRequest request) {
        if (request == null) {
            throw new InvalidPaymentRequestException("Payment request is required");
        }
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new InvalidPaymentRequestException("Order ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentRequestException("Amount must be greater than zero");
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new InvalidPaymentRequestException("Currency is required");
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new InvalidPaymentRequestException("Idempotency key is required");
        }
        if (request.getPaymentMethod() != null && !isSupportedPaymentMethod(request.getPaymentMethod())) {
            throw new InvalidPaymentRequestException("Unsupported payment method: " + request.getPaymentMethod());
        }
    }

    private boolean isSupportedPaymentMethod(PaymentMethod method) {
        return method == PaymentMethod.CARD || method == PaymentMethod.UPI || method == PaymentMethod.NET_BANKING || method == PaymentMethod.WALLET;
    }

    private String generatePaymentId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
