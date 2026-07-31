package com.finance.tracker.chatbot.router;


import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class QuestionClassifier {
    private static final Set<String> KNOWLEDGE_STARTERS = Set.of(
            "what is",
            "what are ",
            "what's",
            "whats",
            "how do",
            "how is",
            "how does",
            "how are",
            "explain",
            "define",
            "describe",
            "tell me about",
            "can you explain",
            "what does",
            "what do",
            "meaning of",
            "difference between",
            "why is",
            "why does",
            "why are",
            "when should",
            "when is"
    );
    private static final Set<String> PERSONAL_SIGNAL = Set.of(
            " My ", " i ", " i've", "i've ", " mine",
            "last month", "this month", "my budget", "my income",
            "my expense", "my saving", "did i spend", "have i spent",
            "am i over", "how much did", "show me my", "me"
    );

    public QuestionType classify(String question) {
        if(question == null || question.isBlank()) {
            return QuestionType.PERSONAL_DATA;
        }
        String q = question.toLowerCase().trim();
        if(PERSONAL_SIGNAL.stream().anyMatch(q::contains)) {
            return QuestionType.PERSONAL_DATA;
        }

        if(KNOWLEDGE_STARTERS.stream().anyMatch(q::startsWith)){
            return QuestionType.GENERAL_KNOWLEDGE;
        }

        return QuestionType.PERSONAL_DATA;
    }
}
