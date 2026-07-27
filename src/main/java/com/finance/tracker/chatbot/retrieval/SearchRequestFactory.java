package com.finance.tracker.chatbot.retrieval;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;

@Component
public class SearchRequestFactory {

    public SearchRequest forUser(String question, String userId) {

        return SearchRequest.builder()
                .query(question)
                .topK(5)
                .filterExpression("userId == '" + userId + "'")
                .build();
    }
}