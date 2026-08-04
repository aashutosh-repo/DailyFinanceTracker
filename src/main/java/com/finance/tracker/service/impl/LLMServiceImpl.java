package com.finance.tracker.service.impl;

import com.finance.tracker.dto.chatbot.OllamaRequest;
import com.finance.tracker.dto.chatbot.OllamaResponse;
import com.finance.tracker.service.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
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

        OllamaRequest request = new OllamaRequest(
                        model,
                        prompt,
                        false,
                        new OllamaRequest.Options(520, 1024, 0.4));

        long start = System.currentTimeMillis();

        OllamaResponse response = webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .block();

        log.info("LLM Response Time in {}ms using model {} ", System.currentTimeMillis()-start, model);

        return response == null ? "" : response.response().trim();
    }

    @Override
    public String askLLM(String prompt) {
        return getLLMResponse(prompt);
    }
}