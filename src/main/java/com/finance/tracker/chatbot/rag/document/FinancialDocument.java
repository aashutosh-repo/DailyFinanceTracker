package com.finance.tracker.chatbot.rag.document;

import lombok.Getter;
import org.springframework.ai.document.Document;

import java.util.UUID;

@Getter
public class FinancialDocument {

    private final UUID id;
    private final Document document;
    private final DocumentType type;

    public FinancialDocument( UUID id, Document document, DocumentType type) {
        this.id = id;
        this.document = document;
        this.type = type;
    }

}