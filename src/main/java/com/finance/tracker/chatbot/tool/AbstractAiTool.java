package com.finance.tracker.chatbot.tool;

public abstract class AbstractAiTool implements AiTool {
    protected String normalize(String question) {
        return question == null ? "" : question.toLowerCase().trim();
    }
}