package com.finance.tracker.service;

public interface LLMService {
    String getLLMResponse(String prompt);
    String askLLM(String prompt);
}
