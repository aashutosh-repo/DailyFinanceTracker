package com.finance.tracker.chatbot.rag.document;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.util.DocumentIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialInsightDocumentBuilder implements FinancialDocumentBuilder{

    private final DocumentIdGenerator idGenerator;

    @Override
    public DocumentType getSupportedType() {
        return DocumentType.FINANCIAL_INSIGHT;
    }

    @Override
    public FinancialDocument build(FinancialContext context) {
        UUID id = idGenerator.generate(context.userId(), context.month(),DocumentType.FINANCIAL_INSIGHT);
        String content = buildContent(context);
        Map<String, Object> metadata = buildMetadata(context);
        Document document = new Document(id.toString(), content,metadata);
        return new FinancialDocument(id, document,DocumentType.FINANCIAL_INSIGHT);
        return null;
    }

    private String buildContent(FinancialContext context) {
        int score = computeHealthScore(context);
        StringBuilder sb = new StringBuilder();
        sb.append("Financial Health Report \n");
        sb.append("==========================");
        sb.append("Reporting Month : ").append(context.month()).append("\n");
        sb.append("Health Score : ").append(score).append(" /100 \n\n");

        appendScoreBreakdown(sb, context, score);
        appendKeyInsights(sb, context);
        appendTrends(sb, context);

        return null;
    }

    private void appendScoreBreakdown(StringBuilder sb, FinancialContext context, int score) {
        sb.append("Score Breakdown \n");
        sb.append("-------------------\n");

        int savingScore = scoreSaving(context.savingsRate());
        sb.append(" Saving Rate : ").append(savingScore).append("/30 pts");
        if(context.savingsRate() != null) {
            sb.append(" - ").append(context.savingsRate()).append("%");
        }
        sb.append("\n");

        int budgetScore = scoreBudget(context.budgetStatuses());
        sb.append(" Budget Adherence : ").append(budgetScore).append("/25 pts");

        int incomeScore = scoreIncomeVsExpenses(context);
        sb.append(" Income Vs Expense : ").append(incomeScore).append("/25 pts");

        int trendScore = scoreTrend(context.comparisons());
        sb.append(" Spending trend : ").append(trendScore).append("/25 pts");

    }


    private void appendKeyInsights(StringBuilder sb, FinancialContext context) {

    }
}
