package com.finance.tracker.payment.notification;

import org.springframework.stereotype.Service;

@Service
public class DummyPaymentNotificationService {

    public void sendEmailNotification(String paymentId, String email, String message) {
        // Simulates sending a payment-linked email notification.
        // Real provider integration can be swapped in without changing the controller/service contract.
    }

    public void sendSmsNotification(String paymentId, String phoneNumber, String message) {
        // Simulates sending a payment-linked SMS notification.
    }
}
