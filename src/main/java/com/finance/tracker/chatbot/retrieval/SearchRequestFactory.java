package com.finance.tracker.chatbot.retrieval;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SearchRequestFactory {
    private static final int DEFAULT_TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.50;
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,50}$");

    public SearchRequest forUser(String question, String userId) {

        if (userId == null || !USER_ID_PATTERN.matcher(userId).matches()){
            throw new IllegalArgumentException("Invalid userId format");
        }

        return SearchRequest.builder()
                .query(question)
                .topK(DEFAULT_TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression("userId == '" + userId + "'")
                .build();
    }
}