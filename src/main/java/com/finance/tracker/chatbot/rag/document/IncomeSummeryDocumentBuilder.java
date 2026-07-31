package com.finance.tracker.chatbot.rag.document;


import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.util.DocumentIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IncomeSummeryDocumentBuilder implements FinancialDocumentBuilder{
    private final DocumentIdGenerator idGenerator;
    private final DocumentType documentType = DocumentType.INCOME_SUMMARY;

    @Override
    public DocumentType getSupportedType() {
        return documentType;
    }

    @Override
    public FinancialDocument build(FinancialContext context) {
        UUID id = idGenerator.generate(context.userId(), context.month(), documentType);
        String content = buildContent(context);
        Map<String , Object> metadata = buildMetadata(context);
        Document document = new Document(id.toString(), content, metadata);
        return new FinancialDocument(id, document, documentType);
    }

    private String buildContent(FinancialContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("INCOME SUMMERY\n");
        sb.append("=====================\n\n");
        sb.append("Reporting Month : ").append(context.month()).append("\n");
        sb.append("Total Income     : $").append(context.totalIncome()).append("\n");
        sb.append("Total Expenses   : $").append(context.totalExpense()).append("\n");
        sb.append("Total Saving     : $").append(context.totalSavings()).append("\n\n");

        if(context.savingsRate() != null){
            sb.append("Saving rate : ").append(context.savingsRate()).append("\n");
        }

        List<CategoryExpense> expenses = context.categoryExpenses();
        if(expenses == null || expenses.isEmpty()) {
            sb.append("No Expenses recorded for this month");
            return sb.toString();
        }

        sb.append("\nFinancial Health\n");
        sb.append("---------------------\n");

        int cmp = context.totalIncome().compareTo(context.totalExpense());

        if(cmp > 0) {
            sb.append("Income exceeds expenses. Monthly surplus of $")
                    .append(context.totalSavings()).append("\n");
        }else if (cmp < 0){
            sb.append("Expenses exceeds Income. Monthly deficit of $")
                    .append(context.totalSavings()).append("\n");
        } else {
            sb.append("Income and expenses are equal monthly");
        }

        if (context.savingsRate() != null){
            if( context.savingsRate().compareTo(BigDecimal.valueOf(20)) >= 0) {
                sb.append("Saving rate of").append(context.savingsRate())
                        .append("% is healthy (above 20% target)");
            } else if (context.savingsRate().compareTo(BigDecimal.ZERO ) > 0) {
                sb.append("Saving rate of").append(context.savingsRate())
                        .append("% is below the recommended 20% target");
            }
        }
        return sb.toString();
    }

    private Map<String, Object> buildMetadata(FinancialContext context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId",context.userId());
        metadata.put("month",context.month().toString());
        metadata.put("year",context.month().getYear());
        metadata.put("documentType",documentType.name());
        metadata.put("businessKey",context.userId()+"_"+context.month()+"_INCOME_SUMMERY");
        metadata.put("indexedAt", Instant.now().toString());
        return metadata;
    }
}

