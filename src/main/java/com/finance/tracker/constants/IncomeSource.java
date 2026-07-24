package com.finance.tracker.constants;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
public enum IncomeSource {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    BUSINESS("Business"),
    INVESTMENT("Investment"),
    GIFT("Gift"),
    OTHER("Others");

    private final String code;

    IncomeSource(String code) {
        this.code = code;
    }

}
