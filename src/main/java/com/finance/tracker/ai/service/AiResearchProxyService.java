package com.finance.tracker.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class AiResearchProxyService {

    private final WebClient webClient;

    public AiResearchProxyService(@Value("${ai.service.url}") String aiServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    public JsonNode chat(String question) {
        return post("/api/ai/chat", question);
    }

    public JsonNode submitJob(String question) {
        return webClient.method(HttpMethod.GET)
                .uri("/api/ai/chat/jobs")
                .bodyValue(Map.of("question", question))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getJob(String jobId) {
        return webClient.get()
                .uri("/api/ai/chat/jobs/{jobId}", jobId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private JsonNode post(String path, String question) {
        try {
            return webClient.post()
                    .uri(path)
                    .bodyValue(Map.of("question", question))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException exception) {
            log.error("AI service returned {} for {}: {}", exception.getStatusCode(), path, exception.getResponseBodyAsString());
            throw exception;
        }
    }
}