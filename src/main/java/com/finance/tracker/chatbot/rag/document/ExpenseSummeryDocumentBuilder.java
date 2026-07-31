package com.finance.tracker.chatbot.rag.document;


import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.util.DocumentIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseSummeryDocumentBuilder implements FinancialDocumentBuilder{
    private final DocumentIdGenerator idGenerator;
    private final DocumentType documentType = DocumentType.EXPENSE_SUMMARY;

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
        sb.append("EXPENSE SUMMERY\n");
        sb.append("=====================\n\n");
        sb.append("Reporting Month : ").append(context.month()).append("\n");
        sb.append("Total Expenses : $").append(context.totalExpense()).append("\n\n");

        List<CategoryExpense> expenses = context.categoryExpenses();
        if(expenses == null || expenses.isEmpty()) {
            sb.append("No Expenses recorded for this month");
            return sb.toString();
        }

        sb.append("category Breakdown\n");
        sb.append("--------------------\n");
        long exceeded = 0;
        expenses.stream()
                .sorted(Comparator.comparing(CategoryExpense::getAmount).reversed())
                .forEach(expense -> {
                        BigDecimal pct = BigDecimal.ZERO;
                        if (context.totalExpense().compareTo(BigDecimal.ZERO) > 0){
                            pct = expense.getAmount()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(context.totalExpense(),2, RoundingMode.HALF_UP);
                        }
                        sb.append("* ").append(expense.getCategory());
                        sb.append(" : $").append(expense.getAmount());
                        sb.append(" ($").append(pct).append("%\n");
                    });
        sb.append("\n");

        expenses.stream()
                .max(Comparator.comparing(CategoryExpense::getAmount))
                .ifPresent(top ->
                    sb.append("Largest spending category: ").append(top.getCategory())
                            .append(" ($").append(top.getAmount()).append(")\n")
                );
        expenses.stream()
                .max(Comparator.comparing(CategoryExpense::getAmount))
                .ifPresent(low ->
                        sb.append("Smallest spending category: ").append(low.getCategory())
                                .append(" ($").append(low.getAmount()).append(")\n")
                );
        return sb.toString();
    }

    private Map<String, Object> buildMetadata(FinancialContext context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId",context.userId());
        metadata.put("month",context.month().toString());
        metadata.put("year",context.month().getYear());
        metadata.put("documentType",documentType.name());
        metadata.put("businessKey",context.userId()+"_"+context.month()+"_EXPENSE_SUMMERY");
        metadata.put("indexedAt", Instant.now().toString());
        return metadata;
    }
}

