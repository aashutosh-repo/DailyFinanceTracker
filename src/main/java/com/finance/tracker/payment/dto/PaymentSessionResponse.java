package com.finance.tracker.payment.dto;

import com.finance.tracker.payment.entity.PaymentSession;

import java.time.LocalDateTime;

public class PaymentSessionResponse {
    private String paymentId;
    private String sessionId;
    private String upiId;
    private String upiUri;
    private String qrPayload;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentSessionResponse fromEntity(PaymentSession session) {
        PaymentSessionResponse response = new PaymentSessionResponse();
        response.setPaymentId(session.getPaymentId());
        response.setSessionId(session.getSessionId());
        response.setUpiId(session.getUpiId());
        response.setUpiUri(session.getUpiUri());
        response.setQrPayload(session.getQrPayload());
        response.setStatus(session.getStatus() == null ? null : session.getStatus().name());
        response.setExpiresAt(session.getExpiresAt());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public String getUpiUri() { return upiUri; }
    public void setUpiUri(String upiUri) { this.upiUri = upiUri; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
