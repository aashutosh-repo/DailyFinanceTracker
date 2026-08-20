package com.finance.tracker.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.stringtemplate.v4.STErrorListener;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialSummeryDTO {
    private String currency;
    private String totalIncome;
    private String totalExpense;
    private String net;

    private Long incomeTransactionCount;
    private Long expenseTransactionCount;
    private Long transferCount;

    private Map<String, String> incomeByCategory;
    private Map<String, String> expenseByCategory;

}
