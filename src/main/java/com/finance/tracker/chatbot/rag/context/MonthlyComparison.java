package com.finance.tracker.chatbot.rag.context;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyComparison {

    private String category;
    private BigDecimal previousAmount;
    private BigDecimal currentAmount;

    public MonthlyComparison() {
    }

    public MonthlyComparison(String category,
                             BigDecimal previousAmount,
                             BigDecimal currentAmount) {

        this.category = category;
        this.previousAmount = previousAmount;
        this.currentAmount = currentAmount;
    }
}