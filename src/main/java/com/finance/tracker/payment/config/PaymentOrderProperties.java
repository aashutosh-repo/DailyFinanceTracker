package com.finance.tracker.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "payment.orders")
public class PaymentOrderProperties {
    private String demoOrderId;
    private BigDecimal demoAmount;

    public String getDemoOrderId() { return demoOrderId; }
    public void setDemoOrderId(String demoOrderId) { this.demoOrderId = demoOrderId; }
    public BigDecimal getDemoAmount() { return demoAmount; }
    public void setDemoAmount(BigDecimal demoAmount) { this.demoAmount = demoAmount; }
}
