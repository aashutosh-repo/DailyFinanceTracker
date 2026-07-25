package com.finance.tracker.constants;

import lombok.Getter;

@Getter
public enum ExpenseType {
    EDUCATION("Education"),
    INVESTMENT("Investment"),
    UTILITY("Utility"),
    SHOPPING("Shopping"),
    GROCERY("Grocery"),
    TRAVELLING("Travelling"),
    OTHER("Others");

    ExpenseType(String education) {
    }
}
