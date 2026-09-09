package com.finance.tracker.payment.persistence;

import com.finance.tracker.payment.enums.PaymentStatus;
import com.finance.tracker.payment.enums.Provider;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "payment_transaction_events")
public class PaymentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 64)
    private String paymentId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Provider provider;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(name = "response_code", length = 64)
    private String responseCode;

    @Column(name = "response_message", length = 500)
    private String responseMessage;

    @ElementCollection
        @CollectionTable(name = "payment_transaction_event_details",
            joinColumns = @JoinColumn(name = "payment_transaction_event_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value", length = 500)
    private Map<String, String> safeDetails = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static PaymentEventEntity create(String paymentId, String eventType, PaymentStatus status,
                                             Provider provider, String providerPaymentId, String responseCode,
                                             String responseMessage, Map<String, String> safeDetails) {
        PaymentEventEntity event = new PaymentEventEntity();
        event.paymentId = paymentId;
        event.eventType = eventType;
        event.status = status;
        event.provider = provider;
        event.providerPaymentId = providerPaymentId;
        event.responseCode = responseCode;
        event.responseMessage = responseMessage;
        if (safeDetails != null) event.safeDetails.putAll(safeDetails);
        return event;
    }
}
