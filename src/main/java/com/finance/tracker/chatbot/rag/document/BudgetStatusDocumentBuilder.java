package com.finance.tracker.chatbot.rag.document;


import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.util.DocumentIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetStatusDocumentBuilder implements FinancialDocumentBuilder{
    private final DocumentIdGenerator idGenerator;
    private final DocumentType documentType = DocumentType.BUDGET_STATUS;


    @Override
    public DocumentType getSupportedType() {
        return documentType;
    }

    @Override
    public FinancialDocument build(FinancialContext context) {
        UUID id = idGenerator.generate(context.userId(),context.month(),DocumentType.BUDGET_STATUS);
        String content = buildContent(context);
        Map<String , Object> metadata = buildMetadata(context);
        Document document = new Document(id.toString(), content, metadata);
        return new FinancialDocument(id, document, documentType);
    }

    private String buildContent(FinancialContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("BUDGET STATUS REPORT\n");
        sb.append("=====================\n\n");
        sb.append("Reporting Month : ").append(context.month()).append("\n\n");

        if(context.budgetStatuses() == null || context.budgetStatuses().isEmpty()) {
            sb.append("No Budget have been defined for this month");
            return sb.toString();
        }

        sb.append("Budget Performance\n");
        sb.append("--------------------\n");
        long exceeded = 0;
        for(BudgetStatus budget : context.budgetStatuses()) {
            sb.append("* ").append(budget.getCategory());
            sb.append(" : $").append(budget.getActual());
            sb.append(" / %").append(budget.getBudget());
            if(budget.isExceeded()) {
                BigDecimal over = budget.getActual().subtract(budget.getBudget());
                sb.append("- EXCEEDED By &").append(over);
            } else {
                BigDecimal usagePct = budget.getBudget().compareTo(BigDecimal.ZERO) > 0
                        ? budget.getActual().multiply(BigDecimal.valueOf(100))
                          .divide(budget.getBudget(), 0, RoundingMode.HALF_UP): BigDecimal.ZERO;
                sb.append("- Within Budget (").append(usagePct).append("% used");
            }
            sb.append("\n");
        }
        sb.append("\nSummary: ").append(exceeded).append(" of ")
                .append(context.budgetStatuses().size()).append(" budgets Exceeded this month \n");
        return sb.toString();
    }

    private Map<String, Object> buildMetadata(FinancialContext context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId",context.userId());
        metadata.put("month",context.month().toString());
        metadata.put("year",context.month().getYear());
        metadata.put("documentType",documentType.name());
        metadata.put("businessKey",context.userId()+"_"+context.month()+"_BUDGET_STATUS");
        metadata.put("indexedAt", Instant.now().toString());
        return metadata;
    }
}

