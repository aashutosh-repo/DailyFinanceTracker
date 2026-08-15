package com.finance.tracker.service.impl;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessTokenStore {

    public static class AccessToken {
        public final String username;
        public final Instant expiresAt;

        public AccessToken(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();

    public String createToken(String username, long ttlSeconds) {
        String id = UUID.randomUUID().toString();
        Instant exp = Instant.now().plusSeconds(ttlSeconds);
        tokens.put(id, new AccessToken(username, exp));
        return id;
    }

    public boolean validate(String token) {
        AccessToken t = tokens.get(token);
        if (t == null) return false;
        if (Instant.now().isAfter(t.expiresAt)) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public String getUsername(String token) {
        AccessToken t = tokens.get(token);
        return t == null ? null : t.username;
    }

    public void revoke(String token) {
        tokens.remove(token);
    }
}

