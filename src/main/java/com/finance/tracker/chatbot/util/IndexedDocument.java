package com.finance.tracker.chatbot.util;

import com.finance.tracker.chatbot.rag.document.DocumentType;
import org.springframework.ai.document.Document;

import java.util.UUID;

public record IndexedDocument(

        UUID id,
        Document document,
        DocumentType type
) {
}