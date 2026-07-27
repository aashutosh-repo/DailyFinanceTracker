package com.finance.tracker.chatbot.indexing;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.rag.document.DocumentFactory;
import com.finance.tracker.chatbot.rag.document.DocumentType;
import com.finance.tracker.chatbot.rag.document.FinancialDocument;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialIndexingServiceImpl implements FinancialIndexingService{

    private final FinancialAnalyticsService analyticsService;
    private final DocumentFactory documentFactory;
    private final VectorStore vectorStore;
    @Override
    public void indexMonthlySummary(String userId, YearMonth month) {
        FinancialContext context =
                analyticsService.getMonthlyContext(userId, month);

        FinancialDocument document =
                documentFactory.create(
                        DocumentType.MONTHLY_SUMMARY,
                        context
                );
        System.out.println("Documents: "+document.getDocument().getText());

        vectorStore.add(
                List.of(document.getDocument())
        );
    }

    @Override
    public void reindexUser(String userId) {

    }
}
