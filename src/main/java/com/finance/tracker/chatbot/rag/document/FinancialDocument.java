package com.finance.tracker.chatbot.rag.document;

import lombok.Getter;
import org.springframework.ai.document.Document;

@Getter
public class FinancialDocument {

    private final Document document;
    private final DocumentType type;

    public FinancialDocument(Document document,
                             DocumentType type) {
        this.document = document;
        this.type = type;
    }

}