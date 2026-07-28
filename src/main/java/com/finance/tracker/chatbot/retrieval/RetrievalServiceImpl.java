package com.finance.tracker.chatbot.retrieval;

import com.finance.tracker.dto.chatbot.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class RetrievalServiceImpl implements RetrievalService{
    private final VectorStore vectorStore;
    private final SearchRequestFactory factory;

    @Override
    public List<Document> retrieveRelevantDocuments(
            String question,
            String userId) {

        SearchRequest request = factory.forUser(question, userId);
        List<Document> documents = vectorStore.similaritySearch(request);

        log.info("Question: {}", question);
        log.info("Retrieved {} documents", documents.size());

        documents.forEach(document -> {
            log.info("----------------------------");
            log.info("Score     : {}", document.getScore());
            log.info("Metadata  : {}", document.getMetadata());
            log.info("Preview   : {}",
                    document.getText().substring(0, Math.min(120, document.getText().length())));
        });

        return documents;
    }
}
