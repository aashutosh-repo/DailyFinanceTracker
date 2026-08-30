package com.finance.tracker.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(EmbeddingStore.class)
public class DocumentsReader {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    public List<TextSegment> readDocument(String filePath) throws Exception {
        String text = Files.readString(
                Paths.get(filePath));

        Document document = Document.from(text);
        DocumentSplitter splitter =
                DocumentSplitters.recursive(300, 0);

        List<TextSegment> segments = splitter.split(document);
        segments.forEach(segment -> System.out.println("Segment: " + segment.text()));
        List<Embedding> embeddings =
                embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);
        return segments;
    }

    public String getAnswer() {
        String query1 = "How is the system modular?";
        Embedding queryEmbedding = embeddingModel.embed(query1).content();
        List<EmbeddingMatch<TextSegment>> results =
                store.findRelevant(queryEmbedding, 5);
        List<EmbeddingMatch<TextSegment>> filtered =
                results.stream()
                        .filter(r -> r.score() > 0.82) // adjust threshold
                        .filter(r -> !r.embedded().text().toLowerCase().contains("table of contents"))
                        .filter(r -> r.embedded().text().length() > 50)
                        .toList();
        return filtered.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }
}
