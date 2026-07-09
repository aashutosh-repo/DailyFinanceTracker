package com.finance.tracker.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaConfig {

    @Bean
    public WebClient ollamaWebClient(
            @Value("${spring.ai.ollama.url}") String baseUrl
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public ChatLanguageModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi3:mini")
                .build();
    }
    @Bean
    EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("mxbai-embed-large")
                .build();
    }
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {

        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5433)
                .database("pgvector")
                .user("postgres")
                .password("postgres")
                .table("documents")
                .dimension(1024)
                .build();
    }
}
