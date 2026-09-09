package com.finance.tracker.payment.persistence;

import com.finance.tracker.payment.entity.Payment;
import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentRecordEntity {
    @Id
    @Column(name = "payment_id", length = 64, nullable = false, updatable = false)
    private String paymentId;

    @Column(name = "order_id", length = 100, nullable = false)
    private String orderId;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Provider provider;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(name = "idempotency_key", nullable = false, length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static PaymentRecordEntity fromDomain(Payment payment) {
        PaymentRecordEntity entity = new PaymentRecordEntity();
        entity.paymentId = payment.getPaymentId();
        entity.orderId = payment.getOrderId();
        entity.customerId = payment.getCustomerId();
        entity.amount = payment.getAmount();
        entity.currency = payment.getCurrency();
        entity.paymentMethod = payment.getPaymentMethod();
        entity.status = payment.getStatus();
        entity.provider = payment.getProvider();
        entity.providerPaymentId = payment.getProviderPaymentId();
        entity.idempotencyKey = payment.getIdempotencyKey();
        entity.createdAt = payment.getCreatedAt();
        entity.updatedAt = payment.getUpdatedAt();
        return entity;
    }

    public Payment toDomain() {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(status);
        payment.setProvider(provider);
        payment.setProviderPaymentId(providerPaymentId);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setCreatedAt(createdAt);
        payment.setUpdatedAt(updatedAt);
        return payment;
    }

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public Provider getProvider() { return provider; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
