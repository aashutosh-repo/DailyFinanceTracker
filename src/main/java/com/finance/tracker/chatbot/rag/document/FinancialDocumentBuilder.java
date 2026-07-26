package com.finance.tracker.chatbot.rag.document;

import com.finance.tracker.chatbot.rag.context.FinancialContext;

//here Strategy Pattern is being used
public interface FinancialDocumentBuilder {
    DocumentType getSupportedType();
    FinancialDocument build(FinancialContext context);
}
