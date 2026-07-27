package com.finance.tracker.chatbot.rag.document;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class MonthlySummaryDocumentBuilder implements FinancialDocumentBuilder{
    @Override
    public DocumentType getSupportedType() {
        return DocumentType.MONTHLY_SUMMARY;
    }

    @Override
    public FinancialDocument build(FinancialContext context) {
        String content = buildContent(context);

        Map<String, Object> metadata = buildMetadata(context);

        Document document = new Document(content, metadata);

        return new FinancialDocument(
                document,
                DocumentType.MONTHLY_SUMMARY
        );
    }

    private String buildContent(FinancialContext context) {
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, context);
        appendExpenseBreakdown(sb, context);
        appendBudgetStatus(sb, context);
        appendFinancialHighlights(sb, context);

        return sb.toString();
    }

    private Map<String, Object> buildMetadata(
            FinancialContext context) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", context.userId());
        metadata.put("month", context.month().toString());
        metadata.put("year", context.month().getYear());
        metadata.put("documentType", DocumentType.MONTHLY_SUMMARY.name());
        metadata.put("createdAt", Instant.now().toString());
        metadata.put("version", 1);
        return metadata;
    }
    private void appendHeader(StringBuilder sb,
                              FinancialContext context) {

        sb.append("MONTHLY FINANCIAL SUMMARY\n");
        sb.append("=========================\n\n");
        sb.append("Reporting Month : ")
                .append(context.month())
                .append("\n");
        sb.append("Total Income    : $")
                .append(context.totalIncome())
                .append("\n");
        sb.append("Total Expenses  : $")
                .append(context.totalExpense())
                .append("\n");
        sb.append("Total Savings   : $")
                .append(context.totalSavings())
                .append("\n");
//        sb.append("Savings Rate    : ")
//                .append(context.savingsRate())
//                .append("%\n\n");
    }
    private void appendExpenseBreakdown(StringBuilder sb,
                                        FinancialContext context) {

        sb.append("Expense Breakdown\n");
        sb.append("-----------------\n");

        BigDecimal totalExpense = context.totalExpense();

        for (CategoryExpense expense : context.categoryExpenses()) {

            BigDecimal percentage = BigDecimal.ZERO;

            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {

                percentage = expense.getAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalExpense, 2, RoundingMode.HALF_UP);
            }

            sb.append("• ")
                    .append(expense.getCategory())
                    .append(" : $")
                    .append(expense.getAmount())
                    .append(" (")
                    .append(percentage)
                    .append("%)")
                    .append("\n");
        }
        sb.append("\n");
    }

    private void appendBudgetStatus(StringBuilder sb,
                                    FinancialContext context) {

        sb.append("Budget Status\n");
        sb.append("-------------\n");

        if (context.budgetStatuses() == null ||
                context.budgetStatuses().isEmpty()) {

            sb.append("No budgets defined.\n\n");
            return;
        }

        for (BudgetStatus budget : context.budgetStatuses()) {
            sb.append("• ")
                    .append(budget.getCategory())
                    .append(" : ");
            if (budget.isExceeded()) {
                sb.append("Exceeded");
            } else {
                sb.append("Within Budget");
            }
            sb.append("\n");
        }
        sb.append("\n");
    }
    private void appendFinancialHighlights(StringBuilder sb,
                                           FinancialContext context) {

        sb.append("Financial Highlights\n");
        sb.append("--------------------\n");

        CategoryExpense largestExpense =
                context.categoryExpenses()
                        .stream()
                        .max(Comparator.comparing(CategoryExpense::getAmount))
                        .orElse(null);

        if (largestExpense != null) {

            sb.append("• Largest spending category is ")
                    .append(largestExpense.getCategory())
                    .append(" ($")
                    .append(largestExpense.getAmount())
                    .append(").\n");
        }

//        if (context.savingsRate() != null) {
//
//            if (context.savingsRate()
//                    .compareTo(BigDecimal.valueOf(20)) >= 0) {
//
//                sb.append("• Savings rate is healthy at ")
//                        .append(context.savingsRate())
//                        .append("%.\n");
//
//            } else {
//
//                sb.append("• Savings rate is below 20%; consider reviewing discretionary spending.\n");
//            }
//        }

        long exceededBudgets = context.budgetStatuses()
                .stream()
                .filter(BudgetStatus::isExceeded)
                .count();

        if (exceededBudgets == 0) {

            sb.append("• No budget limits were exceeded this month.\n");

        } else {

            sb.append("• ")
                    .append(exceededBudgets)
                    .append(" budget category(s) exceeded their limit.\n");
        }

        sb.append("\n");
    }
}
