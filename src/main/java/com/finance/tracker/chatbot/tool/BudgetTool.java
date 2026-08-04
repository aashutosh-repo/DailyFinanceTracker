package com.finance.tracker.chatbot.tool;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import com.finance.tracker.constants.BudgetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BudgetTool extends AbstractAiTool{

    private final FinancialAnalyticsService financialAnalyticsService;

    private static final Set<String> BUDGET_KEYWORDS = Set.of(
            "budget","budgets", "over budget", "limit", "threshold", "exceed", "exceeded","allowance", "allocation"
    );

    @Override
    public String name() {
        return "BudgetTool";
    }

    @Override
    public boolean supports(String question) {
        String q = normalize(question);
        return BUDGET_KEYWORDS.stream().anyMatch(q::contains);
    }

    @Override
    public ToolResult execute(String userId, String question) {
        YearMonth month = YearMonth.now();
        List<BudgetStatus> statuses = financialAnalyticsService.getMonthlyContext(userId, month).budgetStatuses();
        if (statuses == null || statuses.isEmpty()) {
            return ToolResult.builder()
                    .handled(true)
                    .toolName(name())
                    .data("No Budget found for the user: " + userId)
                    .build();
        }

        StringBuilder data = new StringBuilder();
        data.append("Budget Status for ").append(month). append(": \n");

        for (BudgetStatus status : statuses) {
            BigDecimal usagesPct = status.getBudget().compareTo(BigDecimal.ZERO) > 0
                    ? status.getActual().multiply(BigDecimal.valueOf(100))
                      .divide(status.getBudget(),1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            data.append(" \u2022 ").append(status.getCategory())
                    .append(": $").append(status.getActual())
                    .append(": / $").append(status.getBudget())
                    .append(": (").append(usagesPct).append(" %)");

            if(status.isExceeded()) {
                BigDecimal over = status.getActual().subtract(status.getBudget());
                data.append(" \u26a0 Exceeded by $").append(over);
            } else {
                data.append(" \u2713 within Budget");
            }
            data.append("\n");
        }
        long exceededCount = statuses.stream().filter(BudgetStatus::isExceeded).count();
        data.append("\nsummery: ").append(exceededCount).append(" of ")
                .append(statuses.size()).append(" budget(s) exceeded");
        return ToolResult.builder()
                .handled(true)
                .toolName(name())
                .data(data.toString())
                .build();
    }
}
