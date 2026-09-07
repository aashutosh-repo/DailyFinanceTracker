package com.finance.tracker.payment.entity;

import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Payment {
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Provider provider;
    private String providerPaymentId;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.status = PaymentStatus.CREATED;
    }

    public void transitionTo(PaymentStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Next payment status cannot be null");
        }

        if (!isValidTransition(this.status, nextStatus)) {
            throw new IllegalStateException("Invalid state transition from " + this.status + " to " + nextStatus);
        }

        this.status = nextStatus;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isValidTransition(PaymentStatus current, PaymentStatus next) {
        if (current == null) {
            return next == PaymentStatus.CREATED || next == PaymentStatus.PROCESSING;
        }

        Set<PaymentStatus> allowedFromCreated = Set.of(PaymentStatus.INITIATED, PaymentStatus.PROCESSING);
        Set<PaymentStatus> allowedFromInitiated = Set.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.EXPIRED, PaymentStatus.CANCELLED);
        Set<PaymentStatus> allowedFromPending = Set.of(PaymentStatus.PROCESSING, PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.EXPIRED, PaymentStatus.CANCELLED);
        Set<PaymentStatus> allowedFromProcessing = Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.EXPIRED, PaymentStatus.CANCELLED);

        if (current == PaymentStatus.CREATED) {
            return allowedFromCreated.contains(next);
        }
        if (current == PaymentStatus.INITIATED) {
            return allowedFromInitiated.contains(next);
        }
        if (current == PaymentStatus.PENDING) {
            return allowedFromPending.contains(next);
        }
        if (current == PaymentStatus.PROCESSING) {
            return allowedFromProcessing.contains(next);
        }
        return false;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public void setProviderPaymentId(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
