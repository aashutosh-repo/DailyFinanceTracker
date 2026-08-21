package com.finance.tracker.application.query;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetFinancialSummeryQuery {

    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String currency;
}
