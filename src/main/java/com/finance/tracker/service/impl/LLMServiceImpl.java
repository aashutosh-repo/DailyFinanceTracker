package com.finance.tracker.service.impl;

import com.finance.tracker.dto.chatbot.OllamaRequest;
import com.finance.tracker.dto.chatbot.OllamaResponse;
import com.finance.tracker.service.LLMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LLMServiceImpl implements LLMService {

    private final WebClient webClient;
    private final String model;

    public LLMServiceImpl(
            WebClient ollamaWebClient,
            @Value("${spring.ai.ollama.model-chat}") String model) {

        this.webClient = ollamaWebClient;
        this.model = model;
    }

    @Override
    public String getLLMResponse(String prompt) {

        OllamaRequest request =
                new OllamaRequest(
                        model,
                        prompt,
                        false,
                        new OllamaRequest.Options(
                                520,
                                1024,
                                0.4
                        ));

        long start = System.currentTimeMillis();

        OllamaResponse response =
                webClient.post()
                        .uri("/api/generate")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(OllamaResponse.class)
                        .block();

        System.out.println(
                "LLM Execution Time : "
                        + (System.currentTimeMillis() - start)
                        + " ms");

        return response == null ? "" : response.response().trim();
    }

    @Override
    public String askLLM(String prompt) {
        return "";
    }
}