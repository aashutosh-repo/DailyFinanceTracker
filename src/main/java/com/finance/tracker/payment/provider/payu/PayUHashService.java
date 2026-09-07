package com.finance.tracker.payment.provider.payu;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
public class PayUHashService {
    public String generateRequestHash(Map<String, String> fields, String salt) {
        String value = String.join("|",
                value(fields, "key"),
                value(fields, "txnid"),
                value(fields, "amount"),
                value(fields, "productinfo"),
                value(fields, "firstname"),
                value(fields, "email"),
                value(fields, "udf1"),
                value(fields, "udf2"),
                value(fields, "udf3"),
                value(fields, "udf4"),
                value(fields, "udf5"),
                "", "", "", "", "", salt);
        return sha512(value);
    }

    public boolean verifyResponseHash(Map<String, String> fields, String salt) {
        String prefix = fields.getOrDefault("additionalCharges", "");
        String reverse = String.join("|",
            prefix.isBlank() ? salt : prefix + "|" + salt,
                value(fields, "status"),
                "", "", "", "", "", "", "", "", "",
                value(fields, "email"),
                value(fields, "firstname"),
                value(fields, "productinfo"),
                value(fields, "amount"),
                value(fields, "txnid"),
                value(fields, "key"));
        String expected = sha512(reverse);
        return expected.equalsIgnoreCase(value(fields, "hash"));
    }

    private String value(Map<String, String> fields, String key) {
        return fields.getOrDefault(key, "");
    }

    private String sha512(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte valueByte : bytes) {
                result.append(String.format("%02x", valueByte));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-512 is unavailable", exception);
        }
    }
}
