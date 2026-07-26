package com.finance.tracker.chatbot.rag.context;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryExpense {

    private String category;
    private BigDecimal amount;

    public CategoryExpense() {
    }

    public CategoryExpense(String category, BigDecimal amount) {
        this.category = category;
        this.amount = amount;
    }
}