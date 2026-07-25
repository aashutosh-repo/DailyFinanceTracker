package com.finance.tracker.constants;

public enum BudgetType {

    EDUCATION(1),
    INVESTMENT(2),
    UTILITY(3),
    SHOPPING(4),
    GROCERY(5),
    TRAVELLING(6),
    ADVENTURE(7),
    OTHER(8);

    private final int id;

    BudgetType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public static int getExpenseTypeId(String type) {
        return BudgetType.valueOf(type.toUpperCase()).getId();
    }


    public static BudgetType fromId(int id) {
        for (BudgetType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ExpenseType ID: " + id);
    }
}
