package com.finance.tracker.chatbot.tool;

import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.service.TransactionService;
import com.finance.tracker.service.impl.FinancialTransactionReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseTool extends AbstractAiTool{

    private final FinancialTransactionReadService transactionService;

    private static final Set<String> EXPENSE_KEYWORDS = Set.of(
            "expense","expenses", "spend", "spent", "spending", "debit", "outflow",
            "cost", "paid", "purchase", "bought", "how much did", "outgoing"
    );

    @Override
    public String name() {
        return "ExpenseTool";
    }

    @Override
    public boolean supports(String question) {
        String q = normalize(question);
        return EXPENSE_KEYWORDS.stream().anyMatch(q::contains);
    }

    @Override
    public ToolResult execute(String userId, String question) {
        YearMonth month = YearMonth.now();
        //TODO: check monthly check we need data of one month (e.g 1st of July to maybe 20th July not 20 June to 20 JULY)
        BigDecimal totalExpense = transactionService.getTotalExpense(userId,month);
        List<CategoryExpense> categoryExpenses = transactionService.getCategoryExpense(userId, month);
        StringBuilder data = new StringBuilder();
        data.append("Total Expense for ").append(month).append(" : $").append(totalExpense);

        if (categoryExpenses != null) {
            data.append(" \ncategory breakdown : \n");
            categoryExpenses.stream()
                    .sorted(Comparator.comparing(CategoryExpense::getAmount).reversed())
                    .forEach(c -> data.append(" \u2022 ")
                            .append(c.getCategory()).append(": $").append(c.getAmount()).append("\n"));

            categoryExpenses.stream()
                    .max(Comparator.comparing(CategoryExpense::getAmount))
                    .ifPresent(top -> data.append("\nHighest: ")
                            .append(top.getCategory()).append(" ($").append(top.getAmount()).append(")")

                    );
        }

        return ToolResult.builder()
                .handled(true)
                .toolName(name())
                .data(data.toString())
                .build();
    }
}
