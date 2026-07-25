package com.finance.tracker.dto.budget;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetCategoryResponse {
    private Integer id;
    private String name;
    private String colorCode;
}
