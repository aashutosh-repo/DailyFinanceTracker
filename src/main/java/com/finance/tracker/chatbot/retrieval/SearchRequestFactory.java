package com.finance.tracker.chatbot.retrieval;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;

@Component
public class SearchRequestFactory {
    private static final int DEFAULT_TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.25;

    public SearchRequest forUser(String question, String userId) {

        return SearchRequest.builder()
                .query(question)
                .topK(DEFAULT_TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression("userId == '" + userId + "'")
                .build();
    }
}