package com.finance.tracker.chatbot.tool;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IncomeTool extends AbstractAiTool {

    private static final Set<String> INCOME_KEYWORDS = Set.of(
            "income","Salary", "earning", "earnings", "pay", "paycheck", "wage",
            "wages", "made", "received", "revenue", "deposited", "credited"
    );

    private final FinancialAnalyticsService analyticsService;

    @Override
    public String name() {
        return "IncomeTool";
    }

    @Override
    public boolean supports(String question) {
        String q = normalize(question);
        return INCOME_KEYWORDS.stream().anyMatch(q::contains);
    }

    @Override
    public ToolResult execute(String userId, String question) {
        YearMonth month = YearMonth.now();
        FinancialContext context = analyticsService.getMonthlyContext(userId, month);

        return ToolResult.builder()
                .handled(true)
                .toolName(name())
                .data("Total Income for: "+ month +": $"+ context.totalIncome())
                .build();
    }
}
