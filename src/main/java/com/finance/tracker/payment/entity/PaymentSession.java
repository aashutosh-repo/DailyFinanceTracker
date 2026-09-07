package com.finance.tracker.payment.entity;

import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.PaymentSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSession {
    private String sessionId;
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String upiId;
    private String upiUri;
    private String qrPayload;
    private PaymentSessionStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.status = PaymentSessionStatus.CREATED;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public String getUpiUri() { return upiUri; }
    public void setUpiUri(String upiUri) { this.upiUri = upiUri; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public PaymentSessionStatus getStatus() { return status; }
    public void setStatus(PaymentSessionStatus status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
