package com.finance.tracker.chatbot.indexing;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.rag.document.DocumentFactory;
import com.finance.tracker.chatbot.rag.document.DocumentType;
import com.finance.tracker.chatbot.rag.document.FinancialDocument;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import com.finance.tracker.events.ChangeType;
import com.finance.tracker.service.VectorDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialIndexingServiceImpl implements FinancialIndexingService {

    private final FinancialAnalyticsService analyticsService;
    private final DocumentFactory documentFactory;
    private final VectorStore vectorStore;
    private final VectorDocumentService vectorDocumentService;

    @Override
    public void indexMonthlySummary(String userId, YearMonth month) {
        indexDocument(userId, month, DocumentType.MONTHLY_SUMMARY);
    }

    @Override
    public void indexBudgetStatus(String userId, YearMonth month){
        indexDocument(userId, month, DocumentType.BUDGET_STATUS);
    }

    @Override
    public void indexIncomeSummery(String userId, YearMonth month) {
        indexDocument(userId, month, DocumentType.INCOME_SUMMARY);
    }

    @Override
    public void indexExpenseSummery(String userId, YearMonth month) {
        indexDocument(userId, month, DocumentType.EXPENSE_SUMMARY);
    }

    @Override
    public void indexByChangeType(String userId, YearMonth month, ChangeType changeType) {
        log.info("Indexing by changeType {} userId = {} month {}",changeType, userId, month);
        switch (changeType) {
            case TRANSACTION -> {
                indexExpenseSummery(userId, month);
                indexMonthlySummary(userId, month);
                indexDocument(userId, month, DocumentType.FINANCIAL_INSIGHT);
            }
            case INCOME -> {
                indexIncomeSummery(userId, month);
                indexMonthlySummary(userId, month);
                indexDocument(userId, month, DocumentType.FINANCIAL_INSIGHT);
            }
            case BUDGET -> {
                indexBudgetStatus(userId,month);
                indexMonthlySummary(userId, month);
                indexDocument(userId, month, DocumentType.FINANCIAL_INSIGHT);
            }
            default -> {
                indexMonthlySummary(userId,month);
                indexBudgetStatus(userId, month);
                indexExpenseSummery(userId, month);
                indexIncomeSummery(userId, month);
                indexDocument(userId, month, DocumentType.FINANCIAL_INSIGHT);

            }
        }
    }


    @Override
    public void reindexUser(String userId) {
        YearMonth current = YearMonth.now();
        log.info("Starting full reindexing for userId {} covering 13 Months ", userId);
        IntStream.rangeClosed(0,12)
                .mapToObj(current::minusMonths)
                .forEach(
                        month -> {
                            try{
                                indexByChangeType(userId, month, ChangeType.ALL);
                            } catch (Exception e ){
                                log.error("Reindexing Failed for UserId : {} month {} : {}", userId, month, e.getMessage());
                            }
                        }
                );
    }

    private void indexDocument(String userId, YearMonth month, DocumentType documentType) {
        try {
            FinancialContext context = analyticsService.getMonthlyContext(userId, month);
            FinancialDocument document = documentFactory.create(documentType, context);
//            vectorDocumentService.deleteByDocumentType(userId, month, documentType);
            FinancialDocument financialDocument = documentFactory.create(documentType, context);
            vectorStore.delete(List.of(financialDocument.getId().toString()));
            vectorStore.add(List.of(document.getDocument()));
            log.debug("Income {} expense {} for month {}", context.totalIncome(), context.totalExpense(), month);

        } catch (Exception e ) {
            log.error("Failed to index {} for userId {} month {} : {}",
                    documentType, userId, month, e.getMessage(), e);
        }
    }


}
