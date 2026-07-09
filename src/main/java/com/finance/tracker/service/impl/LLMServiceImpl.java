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
            @Value("${spring.ai.ollama.model}") String model
    ) {
        this.webClient = ollamaWebClient;
        this.model = model;
    }

    @Override
    public String getLLMResponse(String prompt) {
        return "";
    }

    public String askLLM(String userPrompt) {

        String prompt = """
                You are a helpful AI assistant.
                Answer clearly and concisely.

                User Question:
                %s
                """.formatted(userPrompt);

        OllamaRequest request =
                new OllamaRequest(model, prompt, false,
                        new OllamaRequest.Options(520, 1024, 0.4));

        String ExecutionStartTime = String.valueOf(System.currentTimeMillis());
        String response =  webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponse.class)
                .map(OllamaResponse::response)
                .block();
        String executionEndTime = String.valueOf(System.currentTimeMillis());
        System.out.println("LLM Execution Time: " + (Long.parseLong(executionEndTime) - Long.parseLong(ExecutionStartTime)) + " ms");
        System.out.println(response);
        return response != null ? response.trim() : "";
    }
}
