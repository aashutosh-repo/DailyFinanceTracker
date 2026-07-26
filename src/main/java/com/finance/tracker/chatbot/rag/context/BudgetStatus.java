package com.finance.tracker.chatbot.rag.context;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetStatus {

    private String category;
    private BigDecimal budget;
    private BigDecimal actual;
    private boolean exceeded;

    public BudgetStatus() {
    }

    public BudgetStatus(String category,
                        BigDecimal budget,
                        BigDecimal actual,
                        boolean exceeded) {

        this.category = category;
        this.budget = budget;
        this.actual = actual;
        this.exceeded = exceeded;
    }
}