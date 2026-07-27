package com.finance.tracker.chatbot.retrieval;

import org.springframework.ai.document.Document;

import java.util.List;

public interface RetrievalService {

    List<Document> retrieveRelevantDocuments(
            String question,
            String userId);
}