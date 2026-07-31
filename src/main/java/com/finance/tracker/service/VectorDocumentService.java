package com.finance.tracker.service;

import com.finance.tracker.chatbot.rag.document.DocumentType;

import java.time.YearMonth;

public interface VectorDocumentService {

    void deleteMonthlySummary(String userId, YearMonth month);
    void deleteAllForUser(String userId);
    void deleteByDocumentType(String userId, YearMonth month, DocumentType type);

}