package com.finance.tracker.payment.dto;

import com.finance.tracker.payment.enums.PaymentMethod;
import com.finance.tracker.payment.enums.Provider;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreatePaymentRequest {
    @NotBlank(message = "Order ID is required")
    @Size(max = 100, message = "Order ID must not exceed 100 characters")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    private PaymentMethod paymentMethod;

    private Provider provider;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 128, message = "Idempotency key must be under 128 chars")
    private String idempotencyKey;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
