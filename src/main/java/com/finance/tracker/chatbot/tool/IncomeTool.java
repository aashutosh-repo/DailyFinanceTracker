package com.finance.tracker.chatbot.tool;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class IncomeTool extends AbstractAiTool {

    private final FinancialAnalyticsService analyticsService;

    @Override
    public String name() {
        return "IncomeTool";
    }

    @Override
    public boolean supports(String question) {

        String q = normalize(question);

        return q.contains("income")
                || q.contains("salary")
                || q.contains("earning")
                || q.contains("earnings");
    }

    @Override
    public ToolResult execute(String userId, String question) {

        // Month parsing will be improved later.
        YearMonth month = YearMonth.now();

        FinancialContext context = analyticsService.getMonthlyContext(userId, month);

        return ToolResult.builder()
                .handled(true)
                .toolName(name())
                .data("Total Income: $" + context.totalIncome())
                .build();
    }
}
