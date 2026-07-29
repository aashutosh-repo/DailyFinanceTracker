package com.finance.tracker.constants;

import lombok.Getter;

@Getter
public enum TransactionCategory {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    BUSINESS("Business"),
    INVESTMENT("Investment"),
    GIFT("Gift"),
    EDUCATION("Education"),
    UTILITY("Utility"),
    SHOPPING("Shopping"),
    GROCERY("Grocery"),
    TRAVELLING("Travelling"),
    OTHER("Others");

    private final String code;
    TransactionCategory(String code) {
        this.code = code;
    }

}
