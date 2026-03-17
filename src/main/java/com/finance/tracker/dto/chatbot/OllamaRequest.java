package com.finance.tracker.dto.chatbot;

public record OllamaRequest(
        String model,
        String prompt,
        boolean stream,
        Options options
) {

    public record Options(
            Integer num_predict,
            Integer num_ctx,
            Double temperature
    ) {}
}