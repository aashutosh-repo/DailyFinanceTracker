package com.finance.tracker.chatbot.rag.document;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.rag.context.MonthlyComparison;
import com.finance.tracker.chatbot.util.DocumentIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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

        return sb.toString();
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

        int budgetScore = scoreBudgets(context.budgetStatuses());
        sb.append(" Budget Adherence : ").append(budgetScore).append("/25 pts");

        int incomeScore = scoreIncomeVsExpenses(context);
        sb.append(" Income Vs Expense : ").append(incomeScore).append("/25 pts");

        int trendScore = scoreTrends(context.comparisons());
        sb.append(" Spending trend : ").append(trendScore).append("/25 pts");

    }


    private void appendKeyInsights(StringBuilder sb, FinancialContext context) {
        sb.append("Key Insights\n");
        sb.append("-------------\n");

        //saving rate insight
        if (context.savingsRate() != null) {
            if (context.savingsRate().compareTo(BigDecimal.valueOf(20)) >= 0) {
                sb.append(" ✅ Excellent! Saving rate of ").append(context.savingsRate())
                        .append("% is above the 20% target.\n");
            } else if (context.savingsRate().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(" ⚠ Saving rate of ").append(context.savingsRate())
                        .append("% - aim for 20% by reducing discretionary spending.\n");
            }
        }

        //Budget Insight
        if (context.budgetStatuses() !=null) {
            long exceeded = context.budgetStatuses().stream().filter(BudgetStatus::isExceeded).count();
            if (exceeded == 0 && !context.budgetStatuses().isEmpty()) {
                sb.append("\n2705 All Budgets are within Limit");
            } else if (exceeded > 0) {
                sb.append("⚠ ").append(exceeded).append(" budget category exceeded their limit. \n");
                context.budgetStatuses().stream()
                        .filter(BudgetStatus::isExceeded)
                        .forEach(b ->{
                            BigDecimal over = b.getActual().subtract(b.getBudget());
                            sb.append("  - ").append(b.getCategory())
                                    .append(" overspent by $").append("\n");
                        });
            }
        }

        //Surpluse/Deficit
        if(context.totalIncome().compareTo(context.totalExpense()) > 0) {
            sb.append(" \u2705 Monthly surplus of $").append(context.totalSavings()).append("\n");
        } else if (context.totalExpense().compareTo(context.totalIncome()) > 0) {
            BigDecimal deficit = context.totalExpense().subtract(context.totalIncome());
            sb.append("❌ Monthlt Deficit of $").append(deficit).append(" - spending exceeds income. \n");
        }

        sb.append("\n");
    }

    private void appendTrends(StringBuilder sb, FinancialContext context) {
        List<MonthlyComparison> comparisons = context.comparisons();
        if (comparisons == null || comparisons.isEmpty()) {
            return;
        }
        sb.append("Month-over-Month Spending Trends\n");
        sb.append("---------------------------------\n");
        long decreased = comparisons.stream()
                .filter(c -> c.getCurrentAmount().compareTo(c.getPreviousAmount()) < 0).count();
        long increased = comparisons.stream()
                .filter(c -> c.getCurrentAmount().compareTo(c.getPreviousAmount()) > 0).count();
        long unchanged = comparisons.size() - increased- decreased;

        sb.append(" ").append(decreased).append(" Categories decreased spending\n");
        sb.append(" ").append(increased).append(" Categories increased spending\n");
        if(unchanged > 0) {
            sb.append(" ").append(unchanged).append(" Categories unchanged \n");
        }
        sb.append("\n");
    }

    private int computeHealthScore(FinancialContext context) {
        return scoreSaving(context.savingsRate())
                + scoreBudgets(context.budgetStatuses())
                + scoreIncomeVsExpenses(context)
                + scoreTrends(context.comparisons());
    }

    private int scoreBudgets(List<BudgetStatus> budgetStatuses) {
        if (budgetStatuses == null || budgetStatuses.isEmpty()) {
            return 15;
        }
        long exceeded = budgetStatuses.stream().filter(BudgetStatus::isExceeded).count();
        if (exceeded == 0) return 25;
        if (exceeded == 1) return 10;
        return 5;
    }

    private int scoreIncomeVsExpenses(FinancialContext context) {
        int cmp = context.totalIncome().compareTo(context.totalExpense());
        if (cmp > 0) return 25;
        if (cmp == 0) return 10;
        return 0;
    }


    private int scoreTrends(List<MonthlyComparison> comparisons) {
        if (comparisons == null || comparisons.isEmpty()) return 10;
        long decreased = comparisons.stream()
                .filter(c -> c.getCurrentAmount().compareTo(c.getPreviousAmount()) < 0).count();

        int pct = (int) (decreased*100/comparisons.size());
        if (pct >= 60) return 20;
        if (pct >= 40) return 13;
        if (pct >= 20) return 7;
        return 0;
    }

    private int scoreSaving(BigDecimal savingRate) {
        if (savingRate == null) return 0;
        if (savingRate.compareTo(BigDecimal.valueOf(20)) >= 0) return 30;
        if (savingRate.compareTo(BigDecimal.valueOf(10)) >= 0) return 20;
        if (savingRate.compareTo(BigDecimal.ZERO) >= 0) return 10;
        return 0;
    }

    private Map<String, Object> buildMetadata(FinancialContext context) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId",context.userId());
        metadata.put("month",context.month().toString());
        metadata.put("year",context.month().getYear());
        metadata.put("documentType",DocumentType.FINANCIAL_INSIGHT.name());
        metadata.put("businessKey",context.userId()+"_"+context.month()+"_FINANCIAL_INSIGHT");
        metadata.put("indexedAt", Instant.now().toString());
        return metadata;
    }

}
