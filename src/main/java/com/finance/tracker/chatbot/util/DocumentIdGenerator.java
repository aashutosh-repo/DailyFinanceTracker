package com.finance.tracker.chatbot.util;

import com.finance.tracker.chatbot.rag.document.DocumentType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.UUID;

@Component
public class DocumentIdGenerator {

    public UUID generate(String userId, YearMonth month, DocumentType type) {
        String key = userId + "|" + month + "|" + type.name();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}