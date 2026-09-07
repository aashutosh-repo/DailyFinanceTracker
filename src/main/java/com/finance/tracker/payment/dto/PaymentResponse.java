package com.finance.tracker.payment.dto;

import com.finance.tracker.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private String provider;
    private String providerPaymentId;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private String checkoutUrl;
    private Map<String, String> checkoutFields;

    public PaymentResponse() {}

    public static PaymentResponse fromEntity(com.finance.tracker.payment.entity.Payment payment, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setCustomerId(payment.getCustomerId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name());
        response.setStatus(payment.getStatus());
        response.setProvider(payment.getProvider() == null ? null : payment.getProvider().name());
        response.setProviderPaymentId(payment.getProviderPaymentId());
        response.setIdempotencyKey(payment.getIdempotencyKey());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setMessage(message);
        return response;
    }

    public static PaymentResponse fromEntity(com.finance.tracker.payment.entity.Payment payment,
                                             String message,
                                             String checkoutUrl,
                                             Map<String, String> checkoutFields) {
        PaymentResponse response = fromEntity(payment, message);
        response.setCheckoutUrl(checkoutUrl);
        response.setCheckoutFields(checkoutFields);
        return response;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public Map<String, String> getCheckoutFields() { return checkoutFields; }
    public void setCheckoutFields(Map<String, String> checkoutFields) { this.checkoutFields = checkoutFields; }
}
