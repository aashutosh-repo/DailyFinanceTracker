package com.finance.tracker.payment.provider.payu;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayUHashServiceTest {
    private final PayUHashService hashService = new PayUHashService();

    @Test
    void generatesSha512RequestHashWithoutReturningSalt() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key", "test-key");
        fields.put("txnid", "PAY-123");
        fields.put("amount", "1768.82");
        fields.put("productinfo", "Finance Tracker payment");
        fields.put("firstname", "Customer");
        fields.put("email", "customer@example.com");

        String hash = hashService.generateRequestHash(fields, "test-salt");

        assertEquals(128, hash.length());
        assertFalse(hash.contains("test-salt"));
    }

    @Test
    void rejectsMissingOrInvalidResponseHash() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key", "test-key");
        fields.put("txnid", "PAY-123");
        fields.put("status", "success");
        fields.put("amount", "1768.82");
        fields.put("productinfo", "Finance Tracker payment");
        fields.put("firstname", "Customer");
        fields.put("email", "customer@example.com");
        fields.put("hash", "invalid");

        assertFalse(hashService.verifyResponseHash(fields, "test-salt"));
    }
}
