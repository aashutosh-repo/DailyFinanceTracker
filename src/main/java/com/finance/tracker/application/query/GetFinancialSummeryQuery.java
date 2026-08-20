package com.finance.tracker.application.query;


import com.finance.tracker.dto.TransactionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
